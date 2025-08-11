# app.py
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from PIL import Image
import io, os, pytesseract
from transformers import AutoTokenizer, AutoModelForTokenClassification, pipeline

# Import your code (no changes needed to your script)
from ocr_ner_v5_pipeline import (
    preprocess_ocr_text,
    load_drugbank_vocab,
    infer_and_format,
    MODEL_DIR, DRUGBANK_CSV,
    TESS_LANG, TESS_CFG, LOWERCASE_AFTER_OCR
)

app = FastAPI(title="OCR + NER API")

# Optional: allow your mobile app origin (adjust as needed)
# from fastapi.middleware.cors import CORSMiddleware
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"], allow_credentials=True,
#     allow_methods=["*"], allow_headers=["*"],
# )

# Set Windows Tesseract path if present
if os.name == "nt":
    default_tess = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
    if os.path.exists(default_tess):
        pytesseract.pytesseract.tesseract_cmd = default_tess

# Load heavy stuff once
@app.on_event("startup")
def _load_once():
    global tokenizer, model, ner_pipe, drugbank_vocab
    tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR, local_files_only=True)
    model = AutoModelForTokenClassification.from_pretrained(MODEL_DIR, local_files_only=True)
    ner_pipe = pipeline("ner", model=model, tokenizer=tokenizer, aggregation_strategy="none")
    drugbank_vocab = load_drugbank_vocab(DRUGBANK_CSV, column="name")

@app.get("/health")
def health():
    return {
        "ok": True,
        "num_labels": getattr(model, "num_labels", None),
        "labels": getattr(model.config, "id2label", None),
    }

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if file.content_type not in {"image/jpeg", "image/png", "image/webp"}:
        raise HTTPException(status_code=400, detail="Please upload a JPEG/PNG/WEBP image.")
    img_bytes = await file.read()
    if len(img_bytes) > 8 * 1024 * 1024:
        raise HTTPException(status_code=413, detail="Image too large (>8MB).")

    # OCR
    pil_img = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    text = pytesseract.image_to_string(pil_img, lang=TESS_LANG, config=TESS_CFG)
    if LOWERCASE_AFTER_OCR:
        text = text.lower()

    # Preprocess + NER + postprocess
    cleaned = preprocess_ocr_text(text)
    result = infer_and_format(cleaned, drugbank_vocab, ner_pipe)

    # Return JSON your app can render
    result["ocrText"] = cleaned  # optional for debugging/UI
    return JSONResponse(result)
