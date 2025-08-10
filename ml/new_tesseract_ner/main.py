from fastapi import FastAPI, UploadFile, File, APIRouter
from fastapi.responses import JSONResponse
from PIL import Image
import pytesseract
import io
from transformers import pipeline
from utils import correct_drug_name, merge_subwords, load_drugbank_vocab, format_output

app = FastAPI(
    title="Medication OCR & NER API",
    version="1.0"
)

router = APIRouter(prefix="/api/medication")

# === Load model and vocab once ===
model_path = "ner_model"
ner_pipeline = pipeline("ner", model=model_path, tokenizer=model_path, aggregation_strategy="none")
drug_vocab = load_drugbank_vocab("drugbank_vocabulary.csv")

@router.post("/predict_image")
async def predict_image(file: UploadFile = File(...)):
    try:
        image = Image.open(io.BytesIO(await file.read()))

        text = pytesseract.image_to_string(image)

        ner_results = ner_pipeline(text)
        merged = merge_subwords(ner_results)

        grouped = {}
        for item in merged:
            label = item["entity"].replace("B-", "").replace("I-", "")
            grouped.setdefault(label, []).append(item["word"])

        result = format_output(grouped, text, drug_vocab)
        return JSONResponse(content=result)

    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"error": str(e)}
        )
    
@app.get("/ping")
def ping():
    return {"status": "running"}    


# Include router AFTER defining the routes
app.include_router(router)
