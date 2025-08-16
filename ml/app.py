from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image
from pathlib import Path
import io, os, uvicorn, pytesseract
from transformers import AutoTokenizer, AutoModelForTokenClassification, pipeline

# Import shared config & helpers from your pipeline module
from ocr_ner_v5_pipeline import (
    preprocess_ocr_text,
    load_drugbank_vocab,
    infer_and_format,
    MODEL_DIR as PIPELINE_MODEL_DIR,
    DRUGBANK_CSV as PIPELINE_DRUGBANK_CSV,
    TESS_LANG, TESS_CFG,
    is_all_upper_words,
)

# ---- Resolve model/data paths (env var override -> pipeline default) ----
MODEL_DIR = Path(os.getenv("MODEL_DIR", PIPELINE_MODEL_DIR)).resolve()
DRUGBANK_CSV = Path(os.getenv("DRUGBANK_CSV", PIPELINE_DRUGBANK_CSV)).resolve()

app = FastAPI(title="Final_ML_model")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=True,
    allow_methods=["*"], allow_headers=["*"],
)

# Set Windows Tesseract path if present (no-op on Linux/EC2 if tesseract in PATH)
if os.name == "nt":
    default_tess = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
    if os.path.exists(default_tess):
        pytesseract.pytesseract.tesseract_cmd = default_tess

# Globals populated on startup
tokenizer = None
model = None
ner_pipe = None
drugbank_vocab = None

# Load model and vocab once on startup
@app.on_event("startup")
def _load_once():
    global tokenizer, model, ner_pipe, drugbank_vocab

    # Sanity checks
    if not MODEL_DIR.exists() or not MODEL_DIR.is_dir():
        raise RuntimeError(f"MODEL_DIR not found or not a directory: {MODEL_DIR}")

    if not DRUGBANK_CSV.exists():
        raise RuntimeError(f"DrugBank CSV not found: {DRUGBANK_CSV}")

    # Strictly local files
    tokenizer = AutoTokenizer.from_pretrained(str(MODEL_DIR), local_files_only=True)
    model = AutoModelForTokenClassification.from_pretrained(str(MODEL_DIR), local_files_only=True)
    ner_pipe = pipeline("ner", model=model, tokenizer=tokenizer, aggregation_strategy="none")

    drugbank_vocab = load_drugbank_vocab(str(DRUGBANK_CSV), column="name")

@app.get("/health")
def health():
    return {
        "ok": all(x is not None for x in (tokenizer, model, ner_pipe, drugbank_vocab)),
        "num_labels": getattr(model, "num_labels", None) if model else None,
        "labels": getattr(model.config, "id2label", None) if model else None,
        "model_dir": str(MODEL_DIR),
        "drugbank_csv": str(DRUGBANK_CSV),
        "tesseract_cmd": getattr(pytesseract.pytesseract, "tesseract_cmd", "system PATH"),
    }

@app.post("/api/medication/predict_image")
async def predict_image(file: UploadFile = File(...)):
    if file.content_type not in {"image/jpeg", "image/jpg", "image/png", "image/webp"}:
        raise HTTPException(status_code=400, detail="Please upload a JPEG/PNG/WEBP image.")

    img_bytes = await file.read()
    if len(img_bytes) > 8 * 1024 * 1024:
        raise HTTPException(status_code=413, detail="Image too large (>8MB).")

    # OCR
    try:
        pil_img = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid image file.")

    ocr_text = pytesseract.image_to_string(pil_img, lang=TESS_LANG, config=TESS_CFG)

    # Lowercase only if all words are uppercase (to match training behavior)
    if is_all_upper_words(ocr_text):
        ocr_text = ocr_text.lower()

    cleaned = preprocess_ocr_text(ocr_text)

    # Lowercase only for the NER input
    text_for_ner = cleaned.lower()

    # Run inference + postprocessing
    try:
        result = infer_and_format(text_for_ner, drugbank_vocab, ner_pipe)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference failed: {e}")

    return JSONResponse(result)

if __name__ == "__main__":
    # Use port 8000 by default (matches your Android client). Change via PORT env.
    port = int(os.getenv("PORT", "8000"))
    uvicorn.run(app, host="0.0.0.0", port=port)
