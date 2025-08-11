#!/usr/bin/env python3
"""
Quick test of current model with improved preprocessing
No retraining required!
"""

from transformers import pipeline, BertTokenizerFast, BertForTokenClassification
import re

def test_current_model_with_improvements():
    """Test current model with improved preprocessing"""
    
    print("=== Testing Current Model with Improved Processing ===")
    
    # Load your current model directly to handle the size mismatch
    model_path = "ner_medication_model4"
    tokenizer = BertTokenizerFast.from_pretrained(model_path)
    
    # Create model with correct number of labels (9 based on the saved weights)
    model = BertForTokenClassification.from_pretrained(
        model_path, 
        num_labels=9,  # Use 9 labels to match the saved weights
        ignore_mismatched_sizes=True
    )
    ner_pipeline = pipeline("ner", model=model, tokenizer=tokenizer, aggregation_strategy="none")
    
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
    
    def enhanced_ner_processing(text, confidence_threshold=0.6):
        """Enhanced NER processing with better noise handling"""
        
        # Preprocess text
        cleaned_text = preprocess_text(text)
        print(f"Cleaned text: {cleaned_text}")
        
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
    
    # Your noisy input
    noisy_text = """KEEP AWAY FROM CHILDREN Total: 15
2 2C-1-112 145 TAB (1/1) eee
= FluVOXAMine MALEATE SOMG TAB

z
<
3
=
S
=
°
5

STEP 3: TAKE HALF TABLET EVERY OTHER NIGHT
TAKE WITH OR AFTER FOOD. AVOID ALCOHOL.

DIONIS WEE YUN RU °y-SKH 4010/2023
XXXXx982H
MDS / SOC1-SKH-23-1505924001 00003WEK

SENGKANG GENERAL

§ at
HOSPITAL, Outpatient Pharmacy
410 Sengkang East Way, Singapore 644886 TEL: 6930 2262"""
    
    print("Original noisy text:")
    print(noisy_text)
    print("\n" + "="*50)
    
    # Test with improved processing
    results = enhanced_ner_processing(noisy_text)
    
    print("\nImproved NER Results:")
    for result in results:
        print(f"{result['word']} -> {result['entity']} (confidence: {result['score']:.3f})")
    
    # Group by entity type
    grouped = {}
    for result in results:
        entity_type = result['entity'].replace('B-', '').replace('I-', '')
        if entity_type not in grouped:
            grouped[entity_type] = []
        grouped[entity_type].append({
            'word': result['word'],
            'score': result['score']
        })
    
    print("\nGrouped Results:")
    for entity_type, items in grouped.items():
        print(f"{entity_type}: {[item['word'] for item in items]}")
    
    # Extract structured information
    print("\nExtracted Information:")
    
    # Medication name
    med_names = grouped.get("MEDICATION_NAME", [])
    if med_names:
        best_med = max(med_names, key=lambda x: x['score'])
        print(f"Medication: {best_med['word']} (confidence: {best_med['score']:.3f})")
    
    # Dosage
    dosages = grouped.get("DOSAGE", [])
    if dosages:
        best_dosage = max(dosages, key=lambda x: x['score'])
        print(f"Dosage: {best_dosage['word']} (confidence: {best_dosage['score']:.3f})")
    
    # Instructions
    instructions = grouped.get("INSTRUCTION", [])
    if instructions:
        print(f"Instructions: {' '.join([inst['word'] for inst in instructions])}")
    
    # Notes
    notes = grouped.get("NOTE", [])
    if notes:
        print(f"Notes: {' '.join([note['word'] for note in notes])}")

if __name__ == "__main__":
    test_current_model_with_improvements()
