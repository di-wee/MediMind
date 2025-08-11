#!/usr/bin/env python3
"""
Test script for the improved NER model on noisy inputs
"""

import sys
import os
sys.path.append('new_tesseract_ner')

from transformers import pipeline, BertTokenizerFast, BertForTokenClassification
import re
import json

def test_current_model():
    """Test the current model to show the issue"""
    print("=== Testing Current Model ===")
    
    # Load the current model
    model_path = "ner_medication_model4"
    
    try:
        ner_pipeline = pipeline("ner", model=model_path, tokenizer=model_path, aggregation_strategy="none")
        
        # Test with noisy input
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
        
        print("Input text:")
        print(noisy_text)
        print("\n" + "="*50)
        
        # Get NER results
        results = ner_pipeline(noisy_text)
        
        print("Raw NER Results (first 20):")
        for i, result in enumerate(results[:20]):
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
            print(f"{entity_type}: {[item['word'] for item in items[:5]]}")
            
    except Exception as e:
        print(f"Error testing current model: {e}")

def test_improved_processing():
    """Test the improved processing approach"""
    print("\n=== Testing Improved Processing ===")
    
    # Load the model
    model_path = "ner_medication_model4"
    ner_pipeline = pipeline("ner", model=model_path, tokenizer=model_path, aggregation_strategy="none")
    
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
    
    # Test with the same noisy input
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
    
    # Apply improved processing
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
    
    print("\nGrouped Results (Improved):")
    for entity_type, items in grouped.items():
        print(f"{entity_type}: {[item['word'] for item in items]}")

def create_training_recommendations():
    """Create recommendations for improving the model"""
    print("\n=== Training Recommendations ===")
    
    recommendations = {
        "data_augmentation": [
            "Add more noisy, complex prescription images to training data",
            "Create synthetic noisy data by adding random text, symbols, and formatting",
            "Include various prescription formats (different pharmacies, hospitals)",
            "Add OCR artifacts simulation (misspelled words, extra characters)"
        ],
        "model_improvements": [
            "Increase max_length to handle longer texts (currently 256)",
            "Use a larger model like bert-large-cased for better performance",
            "Implement sequence labeling with CRF layer for better entity boundaries",
            "Add attention mechanisms to focus on relevant parts of text"
        ],
        "preprocessing_improvements": [
            "Implement better text cleaning for OCR artifacts",
            "Add domain-specific vocabulary for medication terms",
            "Use regex patterns to identify and clean common noise patterns",
            "Implement text segmentation to focus on relevant sections"
        ],
        "training_strategy": [
            "Use weighted loss to handle class imbalance",
            "Implement curriculum learning (start with clean data, gradually add noise)",
            "Use focal loss to focus on hard examples",
            "Implement data augmentation during training"
        ]
    }
    
    for category, items in recommendations.items():
        print(f"\n{category.upper()}:")
        for i, item in enumerate(items, 1):
            print(f"  {i}. {item}")

if __name__ == "__main__":
    # Test current model
    test_current_model()
    
    # Test improved processing
    test_improved_processing()
    
    # Show recommendations
    create_training_recommendations()
