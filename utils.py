import pandas as pd
import difflib
import re

# === Frequency Number Map ===
FREQUENCY_NUM_MAP = {
    "one": 1,
    "once": 1,
    "two": 2,
    "twice": 2,
    "three": 3,
    "thrice": 3
}

# === Load drug vocabulary ===
def load_drugbank_vocab(csv_path, column="name"):
    df = pd.read_csv(csv_path)
    return df[column].dropna().str.lower().unique().tolist()

# === Correct drug name using fuzzy matching ===
def correct_drug_name(name, vocab):
    matches = difflib.get_close_matches(name.lower(), vocab, n=1, cutoff=0.85)
    return matches[0] if matches else name

# === Merge subword tokens ===
def merge_subwords(entities):
    merged = []
    for ent in entities:
        word = ent["word"]
        label = ent["entity"]
        if word.startswith("##") and merged:
            merged[-1]["word"] += word[2:]
        else:
            merged.append({"word": word, "entity": label})
    return merged

# === Convert frequency text to number and leftover text ===
def frequency_to_number_and_instruction(freq_text):
    if not freq_text.strip():
        return None, ""

    lower = freq_text.lower().strip()

    for k, v in FREQUENCY_NUM_MAP.items():
        if k in lower:
            return v, lower.replace(k, "").strip()

    return 1, lower  # Default to 1 if unknown

# === Convert dosage string to number ===
def dosage_to_number(dosage_str):
    word_map = {
        "one": 1,
        "two": 2,
        "three": 3,
        "four": 4,
        "half": 0.5,
        "quarter": 0.25
    }

    words = dosage_str.lower().split()
    for w in words:
        if w.isdigit():
            return int(w)
        if w in word_map:
            return word_map[w]

    return None

# === Final output formatter ===
def format_output(grouped, raw_text, vocab):
    med_name = " ".join(grouped.get("MEDICATION_NAME", []))
    corrected_name = correct_drug_name(med_name, vocab)

    dosage_str = " ".join(grouped.get("DOSAGE", []))
    dosage_num = dosage_to_number(dosage_str)

    freq_str = " ".join(grouped.get("FREQUENCY", []))
    freq_num, freq_extra = frequency_to_number_and_instruction(freq_str)

    instr_text = " ".join(grouped.get("INSTRUCTION", []))
    if freq_extra:
        instr_text = (instr_text + " " + freq_extra).strip()

    return {
        "medicationName": corrected_name,
        "intakeQuantity": dosage_num,
        "frequency": freq_num,
        "instructions": instr_text,
        "notes": " ".join(grouped.get("NOTE", []))
    }

