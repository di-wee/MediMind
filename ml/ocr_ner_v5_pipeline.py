#!/usr/bin/env python3
"""
Image -> Tesseract OCR -> preprocess_ocr_text (UNMODIFIED) -> NER (agg='none')
-> merge subwords -> group IOB -> DrugBank fuzzy-correct -> structured JSON
"""

import os
import re
import json
import difflib
import pandas as pd
from collections import defaultdict
from typing import List, Dict, Any
from PIL import Image
import pytesseract
from transformers import AutoTokenizer, AutoModelForTokenClassification, pipeline

# ======= PATHS / CONFIG =======
IMAGE_PATH   = r"C:\Users\prisc\Downloads\DionisMed.jpeg"
MODEL_DIR    = r"C:\Users\prisc\OneDrive\Desktop\Github\MediMind\ml\new_tesseract_ner\ner_model5"
DRUGBANK_CSV = r"C:\Users\prisc\OneDrive\Desktop\Github\MediMind\ml\drugbank_vocabulary.csv"

TESS_LANG = "eng"
TESS_CFG  = "--oem 3 --psm 6"   # block of text
LOWERCASE_AFTER_OCR = True      # <-- set False if you don't want lowercasing

# Try to auto-detect Tesseract on Windows if not on PATH
if os.name == "nt":
    default_tess = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
    if os.path.exists(default_tess):
        pytesseract.pytesseract.tesseract_cmd = default_tess

# ======= PREPROCESSING (YOUR ORIGINAL VERSION, UNCHANGED) =======
def preprocess_ocr_text(text) -> str:
    if not isinstance(text, str):
        return ""
    # Replace known OCR noise characters
    text = text.replace("â€œ", "").replace("â€", "").replace("â€˜", "").replace("â€™", "'")

    # Remove unwanted symbols (original pattern preserved)
    text = re.sub(r"(â|Â|¢|§|«|©|®|€|“|”|‘|’|™|…|_|=||•|—|–|@|%|<|>|\\|\||~|`)", "", text)

    # Fix common formatting issues
    text = re.sub(r"(\d)(tab/s|tablet[s]?|cap[s]?|capsule[s]?)", r"\1 tablet", text, flags=re.IGNORECASE)
    text = re.sub(r"(\d)(times)", r"\1 times", text, flags=re.IGNORECASE)
    text = re.sub(r"(\d)\s*x\s*(a|per)?\s*day", r"\1 times a day", text, flags=re.IGNORECASE)

    # Normalize known expressions
    replacements = {
        "twice a day": "2 times a day",
        "three times daily": "3 times a day",
        "when necessary": "when needed",
        "when required": "when needed",
    }

    for wrong, correct in replacements.items():
        text = re.sub(rf"\b{wrong}\b", correct, text, flags=re.IGNORECASE)

    # remove irrelevant data
    lines = text.splitlines()
    cleaned = []

    for line in lines:
        # Skip if line contains clinic/address info
        if any(x in line for x in ["clinic", "centre", "hospital", "#", "blk", "building", "road", "s "]):
            continue
        # Skip prices and quantities
        if re.search(r"\bqty\b|\bprice\b|\$\d+|\d+\.\d{2}", line):
            continue
        cleaned.append(line)

    # Remove extra whitespace
    text = re.sub(r"\s+", " ", text)

    return text.strip()

# ======= POSTPROCESSING (YOURS) =======
def load_drugbank_vocab(csv_path, column="name"):
    df = pd.read_csv(csv_path)
    return df[column].dropna().str.lower().unique().tolist()

def correct_drug_name(name, drugbank_vocab):
    matches = difflib.get_close_matches(name.lower(), drugbank_vocab, n=1, cutoff=0.7)
    return matches[0] if matches else name

def word_to_number(word):
    word_map = {
        "one": 1, "1": 1,
        "two": 2, "2": 2,
        "three": 3, "3": 3,
        "four": 4, "4": 4,
        "half": 0.5, "quarter": 0.25
    }
    return word_map.get(word.lower())

def merge_subwords(entities):
    merged = []
    for ent in entities:
        if ent["word"].startswith("##") and merged:
            merged[-1]["word"] += ent["word"][2:]
        else:
            merged.append(ent.copy())
    return merged

def group_entities_by_label(entities):
    grouped = defaultdict(list)
    current_label = None
    current_words = []

    for ent in entities:
        tag = ent["entity"]
        prefix, label = tag.split("-") if "-" in tag else ("O", tag)

        if prefix == "B":
            if current_label and current_words:
                grouped[current_label].append(" ".join(current_words))
            current_label = label
            current_words = [ent["word"]]
        elif prefix == "I" and label == current_label:
            current_words.append(ent["word"])
        else:
            if current_label and current_words:
                grouped[current_label].append(" ".join(current_words))
            current_label = None
            current_words = []

            if prefix == "B":
                current_label = label
                current_words = [ent["word"]]

    if current_label and current_words:
        grouped[current_label].append(" ".join(current_words))

    return grouped

def clean_text(text):
    return text.replace(" ##", "").replace("##", "").strip()

def infer_and_format(text, drugbank_vocab, ner_pipeline):
    raw_output = ner_pipeline(text)
    merged_output = merge_subwords(raw_output)
    grouped_output = group_entities_by_label(merged_output)

    final = {}

    # Medication Name
    meds = grouped_output.get("MEDICATION_NAME", [])
    if meds:
        med_name = clean_text(meds[0])
        corrected = correct_drug_name(med_name, drugbank_vocab)
        final["medicationName"] = corrected

    # Dosage or Quantity
    dosages = grouped_output.get("DOSAGE", [])
    quantity = 0
    for d in dosages:
        for word in d.split():
            num = word_to_number(word)
            if num is not None:
                quantity = num
                break
        if quantity: break
    final["intakeQuantity"] = quantity

    # Frequency
    freqs = grouped_output.get("FREQUENCY", [])
    freq_number = 0
    for f in freqs:
        for word in f.split():
            num = word_to_number(word)
            if num is not None:
                freq_number = num
                break
        if freq_number: break
    final["frequency"] = freq_number

    # Instructions
    instr = grouped_output.get("INSTRUCTION", [])
    if instr:
        final["instructions"] = clean_text(" ".join(instr))

    # Notes
    notes = grouped_output.get("NOTE", [])
    if notes:
        final["notes"] = clean_text(" ".join(notes))

    # Save to JSON
    with open("ner_v5_output1.json", "w", encoding="utf-8") as f:
        json.dump(final, f, indent=4, ensure_ascii=False)

    return final

# ======= MAIN =======
def main():
    # 1) OCR
    if not os.path.exists(IMAGE_PATH):
        raise FileNotFoundError(f"Image not found: {IMAGE_PATH}")
    img = Image.open(IMAGE_PATH).convert("RGB")
    ocr_text = pytesseract.image_to_string(img, lang=TESS_LANG, config=TESS_CFG)
    if LOWERCASE_AFTER_OCR:
        ocr_text = ocr_text.lower()

    print("=== OCR raw ===")
    print(ocr_text)

    # 2) Preprocess (your original function)
    cleaned_text = preprocess_ocr_text(ocr_text)
    print("\n=== Cleaned text ===")
    print(cleaned_text)

    # 3) NER (agg='none' so your merge_subwords + grouping work)
    tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR, local_files_only=True)
    model = AutoModelForTokenClassification.from_pretrained(MODEL_DIR, local_files_only=True)
    print("\nModel num_labels:", model.num_labels)
    print("Model id2label:", model.config.id2label)
    ner_pipe = pipeline("ner", model=model, tokenizer=tokenizer, aggregation_strategy="none")

    # 4) DrugBank vocab
    if not os.path.exists(DRUGBANK_CSV):
        raise FileNotFoundError(f"DrugBank CSV not found: {DRUGBANK_CSV}")
    vocab = load_drugbank_vocab(DRUGBANK_CSV, column="name")

    # 5) Inference + postprocessing
    final = infer_and_format(cleaned_text, vocab, ner_pipe)

    print("\n=== Final JSON ===")
    print(json.dumps(final, indent=4, ensure_ascii=False))

if __name__ == "__main__":
    main()
