import os
import re
import json
import difflib
import pandas as pd
from collections import defaultdict
from typing import List, Dict, Any
from pathlib import Path
from PIL import Image
import pytesseract
from transformers import AutoTokenizer, AutoModelForTokenClassification, pipeline

# === Base directory = folder where this file lives ===
BASE_DIR = Path(__file__).resolve().parent

# === Paths (relative by default, overridable via environment variables) ===
IMAGE_PATH   = os.getenv("IMAGE_PATH", str(BASE_DIR / "sample.jpg"))   # change default filename if needed
MODEL_DIR    = os.getenv("MODEL_DIR", str(BASE_DIR / "ner_model"))
DRUGBANK_CSV = os.getenv("DRUGBANK_CSV", str(BASE_DIR / "drugbank_vocabulary.csv"))

TESS_LANG = "eng"
TESS_CFG  = "--oem 3 --psm 6"
LOWERCASE_AFTER_OCR = False

# Try to auto-detect Tesseract on Windows if not on PATH
if os.name == "nt":
    default_tess = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
    if os.path.exists(default_tess):
        pytesseract.pytesseract.tesseract_cmd = default_tess

# ------------------- Preprocessing -------------------
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
        "when necessary": "when needed",
        "when required": "when needed",
    }
    for wrong, correct in replacements.items():
        text = re.sub(rf"\b{wrong}\b", correct, text, flags=re.IGNORECASE)

    # remove irrelevant data
    lines = text.splitlines()
    cleaned = []
    for line in lines:
        if any(x in line for x in ["clinic", "centre", "hospital", "#", "blk", "building", "road", "s "]):
            continue
        if re.search(r"\bqty\b|\bprice\b|\$\d+|\d+\.\d{2}", line):
            continue
        cleaned.append(line)

    # keep only letters, numbers, and spaces
    text = re.sub(r"[^A-Za-z0-9\s]", " ", text)

    # Remove extra whitespace
    text = re.sub(r"\s+", " ", text)

    return text.strip()

# ------------------- Postprocessing -------------------
def load_drugbank_vocab(csv_path, column="name"):
    df = pd.read_csv(csv_path)
    return df[column].dropna().str.lower().unique().tolist()

def is_all_upper_words(text: str) -> bool:
    tokens = re.findall(r"[A-Za-z]+", text)
    if not tokens:
        return False
    return all(t.isupper() for t in tokens)

def correct_drug_name_caseaware(name: str, drugbank_vocab, cutoff: float = 0.8):
    lower_to_cased = {v.lower(): v for v in drugbank_vocab}
    lower = name.lower()
    if lower in lower_to_cased:
        return lower_to_cased[lower], True
    cand = difflib.get_close_matches(lower, list(lower_to_cased.keys()), n=1, cutoff=cutoff)
    if cand:
        return lower_to_cased[cand[0]], True
    return name, False

def word_to_number(word):
    word_map = {
        "one": 1, "1": 1, "once": 1,
        "two": 2, "2": 2, "twice": 2,
        "three": 3, "3": 3, "thrice": 3,
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

def find_medication_in_text(text: str, drugbank_vocab, cutoff: float = 0.8):
    txt = text.lower()
    # 1) Exact substring matches
    best_exact = None
    for name in drugbank_vocab:
        if name in txt:
            if best_exact is None or len(name) > len(best_exact):
                best_exact = name
    if best_exact:
        return best_exact
    # 2) Fuzzy match on 1–3 word windows
    tokens = re.findall(r"[a-z][a-z\-]+", txt)
    seen = set()
    for i in range(len(tokens)):
        for n in (3, 2, 1):
            if i + n <= len(tokens):
                cand = " ".join(tokens[i:i+n])
                if cand in seen:
                    continue
                seen.add(cand)
                match = difflib.get_close_matches(cand, drugbank_vocab, n=1, cutoff=cutoff)
                if match:
                    return match[0]
    return None

def infer_and_format(text, drugbank_vocab, ner_pipeline):
    raw_output = ner_pipeline(text)
    merged_output = merge_subwords(raw_output)
    grouped_output = group_entities_by_label(merged_output)
    final = {}
    meds = grouped_output.get("MEDICATION_NAME", [])
    if meds:
        med_name = clean_text(meds[0])
        corrected, matched = correct_drug_name_caseaware(med_name, drugbank_vocab, cutoff=0.8)
        final["medicationName"] = corrected if matched else med_name
    else:
        fallback = find_medication_in_text(text, drugbank_vocab, cutoff=0.8)
        if fallback:
            final["medicationName"] = fallback
    # Intake quantity
    dosages = grouped_output.get("DOSAGE", [])
    quantity = 0
    for d in dosages:
        for word in d.split():
            num = word_to_number(word)
            if num is not None:
                quantity = num
                break
        if quantity:
            break
    final["intakeQuantity"] = quantity
    # Frequency
    freq_phrases = grouped_output.get("FREQUENCY", [])
    freq_nums = []
    freq_words = []
    for phrase in freq_phrases:
        for w in phrase.split():
            lw = w.lower()
            if lw in {"times", "time"}:
                continue
            if w.isnumeric():
                freq_nums.append(int(w))
            else:
                val = word_to_number(w)
                if val is not None:
                    freq_nums.append(val)
                else:
                    freq_words.append(w)
    if freq_nums:
        final["frequency"] = freq_nums[0]
    elif freq_words:
        final["frequency"] = 1
    else:
        final["frequency"] = 0
    # Instructions
    instr_tokens = freq_words + grouped_output.get("INSTRUCTION", [])
    if instr_tokens:
        final["instructions"] = clean_text(" ".join(instr_tokens))
    # Notes
    notes = grouped_output.get("NOTE", [])
    if notes:
        final["notes"] = clean_text(" ".join(notes))
    # Save JSON
    with open("ner_v5_output1.json", "w", encoding="utf-8") as f:
        json.dump(final, f, indent=4, ensure_ascii=False)
    return final

def main():
    # OCR
    if not os.path.exists(IMAGE_PATH):
        raise FileNotFoundError(f"Image not found: {IMAGE_PATH}")
    img = Image.open(IMAGE_PATH).convert("RGB")
    ocr_text = pytesseract.image_to_string(img, lang=TESS_LANG, config=TESS_CFG)
    if is_all_upper_words(ocr_text):
        ocr_text = ocr_text.lower()
    print("=== OCR raw ===")
    print(ocr_text)
    cleaned_text = preprocess_ocr_text(ocr_text)
    print("\n=== Cleaned text ===")
    print(cleaned_text)
    # NER
    tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR, local_files_only=True)
    model = AutoModelForTokenClassification.from_pretrained(MODEL_DIR, local_files_only=True)
    print("\nModel num_labels:", model.num_labels)
    print("Model id2label:", model.config.id2label)
    ner_pipe = pipeline("ner", model=model, tokenizer=tokenizer, aggregation_strategy="none")
    # DrugBank vocab
    if not os.path.exists(DRUGBANK_CSV):
        raise FileNotFoundError(f"DrugBank CSV not found: {DRUGBANK_CSV}")
    vocab = load_drugbank_vocab(DRUGBANK_CSV, column="name")
    # Inference
    text_for_ner = cleaned_text.lower()
    final = infer_and_format(text_for_ner, vocab, ner_pipe)
    print("\n=== Final JSON ===")
    print(json.dumps(final, indent=4, ensure_ascii=False))

if __name__ == "__main__":
    main()
