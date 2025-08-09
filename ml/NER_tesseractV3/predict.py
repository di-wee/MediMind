from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
import uvicorn
import pytesseract
from PIL import Image
import io
from transformers import pipeline
from collections import defaultdict

app = FastAPI()

# Load NER model
model_path = "./model"
ner_pipe = pipeline("ner", model=model_path, tokenizer=model_path, aggregation_strategy="none")

# === Helper functions ===
def word_to_number(word):
    word_map = {
        "one": 1, "two": 2, "three": 3, "four": 4,
        "half": 0.5, "quarter": 0.25
    }
    return word_map.get(word.lower())

def merge_subwords(entities):
    merged_tokens = []
    for ent in entities:
        word = ent["word"]
        entity = ent["entity"]
        if word.startswith("##") and merged_tokens:
            merged_tokens[-1]["word"] += word[2:]
        else:
            merged_tokens.append({"word": word, "entity": entity})
    return merged_tokens

def ner_postprocess(text):
    results = ner_pipe(text)
    merged_tokens = merge_subwords(results)

    grouped_output = defaultdict(list)
    for ent in merged_tokens:
        label = ent["entity"]
        base_label = label.split("-")[-1] if "-" in label else label
        grouped_output[base_label].append(ent["word"])

    custom = {}
    meds = grouped_output.get("MEDICATION_NAME", [])
    if meds:
        custom["medication_name"] = meds[0]

    # Dosage
    dosages = grouped_output.get("DOSAGE", [])
    custom["dosage"] = 0
    for w in dosages:
        if w.isnumeric():
            custom["dosage"] = int(w)
            break
        val = word_to_number(w)
        if val is not None:
            custom["dosage"] = val
            break

    # Frequency
    freq_tokens = grouped_output.get("FREQUENCY", [])
    freq_num = []
    freq_words = []
    for w in freq_tokens:
        if w.lower() in {"times", "time"}:
            continue
        if w.isnumeric():
            freq_num.append(int(w))
        else:
            val = word_to_number(w)
            if val is not None:
                freq_num.append(val)
            else:
                freq_words.append(w)

    custom["frequency"] = freq_num[0] if freq_num else (1 if freq_words else 0)

    # Instruction
    instruction_raw = freq_words + grouped_output.get("INSTRUCTION", [])
    instruction_tokens = []
    for token in instruction_raw:
        if token.startswith("##") and instruction_tokens:
            instruction_tokens[-1] += token[2:]
        else:
            instruction_tokens.append(token)
    if instruction_tokens:
        custom["instruction"] = " ".join(instruction_tokens)

    # Note
    note_tokens = grouped_output.get("NOTE", [])
    note_cleaned = []
    for token in note_tokens:
        if token.startswith("##") and note_cleaned:
            note_cleaned[-1] += token[2:]
        else:
            note_cleaned.append(token)
    if note_cleaned:
        custom["note"] = " ".join(note_cleaned)

    return custom

# === New endpoint to upload image and extract info ===
@app.post("/api/medication/predict_image")
async def predict_image(file: UploadFile = File(...)):
    # Read image bytes
    img_bytes = await file.read()
    img = Image.open(io.BytesIO(img_bytes)).convert("RGB")

    # OCR to extract text
    text = pytesseract.image_to_string(img)

    # Pass to NER
    extracted_info = ner_postprocess(text)

    return {
        "ocr_text": text,
        "extracted_info": extracted_info
    }

# === Run locally ===
if __name__ == "__main__":
    uvicorn.run("predict:app", host="0.0.0.0", port=8000, reload=True)
