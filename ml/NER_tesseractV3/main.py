from fastapi import FastAPI, Request
from pydantic import BaseModel
from transformers import pipeline
from collections import defaultdict
import json

app = FastAPI()

# Load the NER model pipeline
ner_pipeline = pipeline("ner", model="./model", tokenizer="./model", aggregation_strategy="none")

# Request input model
class TextInput(BaseModel):
    text: str

# Helper to clean prediction
def clean_prediction(grouped_output):
    final = {}

    meds = grouped_output.get("MEDICATION_NAME", [])
    if meds:
        final["medication_name"] = meds[0]

    dosages = grouped_output.get("DOSAGE", [])
    filtered = [w for w in dosages if w.isnumeric()]
    final["dosage"] = filtered[0] if filtered else (dosages[0] if dosages else "")

    for field in ["FREQUENCY", "INSTRUCTION", "NOTE"]:
        value = " ".join(grouped_output.get(field, []))
        if value:
            final[field.lower()] = value

    return final

# Inference endpoint
@app.post("/predict")
async def predict(data: TextInput):
    results = ner_pipeline(data.text)
    grouped = defaultdict(list)

    for entity in results:
        label = entity["entity"]
        word = entity["word"]
        base_label = label.split("-")[-1] if "-" in label else label
        grouped[base_label].append(word)

    final_output = clean_prediction(grouped)
    return {"extracted_info": final_output}
