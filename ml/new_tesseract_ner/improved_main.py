from fastapi import FastAPI, UploadFile, File, APIRouter
from fastapi.responses import JSONResponse
from PIL import Image
import pytesseract
import io
from transformers import pipeline, BertTokenizerFast, BertForTokenClassification
from utils import correct_drug_name, merge_subwords, load_drugbank_vocab, format_output
import re
import json

app = FastAPI(
    title="Improved Medication OCR & NER API",
    version="2.0"
)

router = APIRouter(prefix="/api/medication")

# === Load model and vocab once ===
# Use the correct model path from your training
model_path = "../ner_medication_model4"  # Path to your trained model
ner_pipeline = pipeline("ner", model=model_path, tokenizer=model_path, aggregation_strategy="none")
drug_vocab = load_drugbank_vocab("drugbank_vocabulary.csv")

# === Enhanced text preprocessing ===
def preprocess_text(text):
    """Clean and preprocess OCR text to reduce noise"""
    # Remove excessive whitespace and newlines
    text = re.sub(r'\n+', ' ', text)
    text = re.sub(r'\s+', ' ', text)
    
    # Remove common OCR artifacts
    text = re.sub(r'[^\w\s\-\.\,\:\;\(\)\/]', '', text)
    
    # Normalize common medication-related terms
    text = re.sub(r'\bTAB\b', 'TABLET', text, flags=re.IGNORECASE)
    text = re.sub(r'\bMG\b', 'MG', text)
    text = re.sub(r'\bML\b', 'ML', text)
    
    return text.strip()

# === Enhanced NER processing ===
def enhanced_ner_processing(text, confidence_threshold=0.6):
    """Enhanced NER processing with better noise handling"""
    
    # Preprocess text
    cleaned_text = preprocess_text(text)
    
    # Get NER results
    ner_results = ner_pipeline(cleaned_text)
    
    # Filter by confidence and merge subwords
    filtered_results = []
    current_entity = None
    
    for result in ner_results:
        if result['score'] >= confidence_threshold:
            word = result['word']
            entity = result['entity']
            
            # Handle subword tokens
            if word.startswith('##'):
                if current_entity:
                    current_entity['word'] += word[2:]
            else:
                if current_entity:
                    filtered_results.append(current_entity)
                current_entity = {
                    'word': word,
                    'entity': entity,
                    'score': result['score']
                }
    
    if current_entity:
        filtered_results.append(current_entity)
    
    return filtered_results

# === Enhanced entity grouping ===
def enhanced_entity_grouping(ner_results):
    """Enhanced entity grouping with better logic"""
    
    grouped = {}
    
    for item in ner_results:
        # Extract entity type (remove B- and I- prefixes)
        entity_type = item["entity"].replace("B-", "").replace("I-", "")
        
        # Skip low-confidence predictions
        if item["score"] < 0.6:
            continue
            
        # Group by entity type
        if entity_type not in grouped:
            grouped[entity_type] = []
        
        # Avoid duplicates
        if item["word"] not in [existing["word"] for existing in grouped[entity_type]]:
            grouped[entity_type].append({
                "word": item["word"],
                "score": item["score"]
            })
    
    return grouped

# === Enhanced output formatting ===
def enhanced_format_output(grouped, raw_text, vocab):
    """Enhanced output formatting with better extraction logic"""
    
    # Extract medication name (prioritize higher confidence)
    med_names = grouped.get("MEDICATION_NAME", [])
    if med_names:
        # Sort by confidence and take the best
        med_names.sort(key=lambda x: x["score"], reverse=True)
        med_name = med_names[0]["word"]
        corrected_name = correct_drug_name(med_name, vocab)
    else:
        corrected_name = ""
    
    # Extract dosage
    dosages = grouped.get("DOSAGE", [])
    dosage_num = None
    if dosages:
        dosage_str = dosages[0]["word"]
        # Try to extract numeric value
        import re
        numbers = re.findall(r'\d+(?:\.\d+)?', dosage_str)
        if numbers:
            dosage_num = float(numbers[0])
    
    # Extract frequency
    frequencies = grouped.get("FREQUENCY", [])
    freq_num = None
    if frequencies:
        freq_str = frequencies[0]["word"]
        # Map common frequency terms
        freq_mapping = {
            "once": 1, "one": 1, "daily": 1,
            "twice": 2, "two": 2, "bid": 2,
            "three": 3, "thrice": 3, "tid": 3,
            "four": 4, "qid": 4
        }
        freq_lower = freq_str.lower()
        for term, num in freq_mapping.items():
            if term in freq_lower:
                freq_num = num
                break
    
    # Extract instructions
    instructions = grouped.get("INSTRUCTION", [])
    instr_text = " ".join([inst["word"] for inst in instructions])
    
    # Extract notes
    notes = grouped.get("NOTE", [])
    notes_text = " ".join([note["word"] for note in notes])
    
    return {
        "medicationName": corrected_name,
        "intakeQuantity": dosage_num,
        "frequency": freq_num,
        "instructions": instr_text,
        "notes": notes_text,
        "confidence": {
            "medicationName": med_names[0]["score"] if med_names else 0,
            "dosage": dosages[0]["score"] if dosages else 0,
            "frequency": frequencies[0]["score"] if frequencies else 0,
            "instructions": sum(inst["score"] for inst in instructions) / len(instructions) if instructions else 0
        }
    }

@router.post("/predict_image")
async def predict_image(file: UploadFile = File(...)):
    try:
        # Read and process image
        image = Image.open(io.BytesIO(await file.read()))
        
        # Extract text using OCR
        text = pytesseract.image_to_string(image)
        
        # Enhanced NER processing
        ner_results = enhanced_ner_processing(text)
        
        # Enhanced entity grouping
        grouped = enhanced_entity_grouping(ner_results)
        
        # Enhanced output formatting
        result = enhanced_format_output(grouped, text, drug_vocab)
        
        # Add debug information
        result["debug"] = {
            "raw_text": text[:500] + "..." if len(text) > 500 else text,
            "ner_results_count": len(ner_results),
            "grouped_entities": {k: len(v) for k, v in grouped.items()}
        }
        
        return JSONResponse(content=result)
        
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"error": str(e), "type": "processing_error"}
        )

@router.post("/predict_text")
async def predict_text(text: str):
    """Direct text prediction endpoint for testing"""
    try:
        # Enhanced NER processing
        ner_results = enhanced_ner_processing(text)
        
        # Enhanced entity grouping
        grouped = enhanced_entity_grouping(ner_results)
        
        # Enhanced output formatting
        result = enhanced_format_output(grouped, text, drug_vocab)
        
        # Add debug information
        result["debug"] = {
            "ner_results": ner_results,
            "grouped_entities": grouped
        }
        
        return JSONResponse(content=result)
        
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"error": str(e), "type": "processing_error"}
        )

@app.get("/ping")
def ping():
    return {"status": "running", "model": "improved_ner_v2"}

@app.get("/model_info")
def model_info():
    """Get information about the loaded model"""
    try:
        # Load model config to get label information
        config_path = f"{model_path}/config.json"
        with open(config_path, 'r') as f:
            config = json.load(f)
        
        return {
            "model_path": model_path,
            "labels": config.get("id2label", {}),
            "num_labels": config.get("num_labels", 0),
            "model_type": config.get("model_type", "unknown")
        }
    except Exception as e:
        return {"error": str(e)}

# Include router AFTER defining the routes
app.include_router(router)
