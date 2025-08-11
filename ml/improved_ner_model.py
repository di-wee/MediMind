import json
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from collections import Counter
from datasets import Dataset, DatasetDict
from transformers import BertTokenizerFast, BertForTokenClassification
from transformers import TrainingArguments, Trainer, pipeline, EarlyStoppingCallback
from seqeval.metrics import classification_report, accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
import re
import random

# === 1. Enhanced Data Loading and Preprocessing ===
def load_and_preprocess_data(json_path):
    """Load data and apply preprocessing to handle noisy inputs"""
    with open(json_path, "r") as f:
        data = json.load(f)
    
    # Filter out very short or very long sequences
    filtered_data = []
    for sample in data:
        tokens = sample["tokens"]
        labels = sample["labels"]
        
        # Keep sequences between 5 and 200 tokens
        if 5 <= len(tokens) <= 200:
            filtered_data.append(sample)
    
    print(f"Filtered data from {len(data)} to {len(filtered_data)} samples")
    return filtered_data

# === 2. Data Augmentation for Noisy Inputs ===
def augment_noisy_data(data, augmentation_factor=2):
    """Create noisy versions of training data to improve robustness"""
    augmented_data = []
    
    for sample in data:
        tokens = sample["tokens"]
        labels = sample["labels"]
        
        # Add original sample
        augmented_data.append(sample)
        
        # Create noisy versions
        for _ in range(augmentation_factor):
            noisy_tokens = tokens.copy()
            noisy_labels = labels.copy()
            
            # Add random noise tokens (10% chance per position)
            for i in range(len(noisy_tokens)):
                if random.random() < 0.1:
                    # Insert random noise words
                    noise_words = ["TOTAL:", "REF:", "DATE:", "ID:", "PHARMACY:", "HOSPITAL:"]
                    noise_word = random.choice(noise_words)
                    noisy_tokens.insert(i, noise_word)
                    noisy_labels.insert(i, "O")  # Noise tokens are always O
            
            # Add random spacing and formatting
            if random.random() < 0.3:
                # Add random line breaks and formatting
                for i in range(len(noisy_tokens)):
                    if random.random() < 0.05:
                        noisy_tokens[i] = f"\n{noisy_tokens[i]}"
            
            augmented_data.append({
                "tokens": noisy_tokens,
                "labels": noisy_labels
            })
    
    return augmented_data

# === 3. Enhanced Label Mapping ===
label_list = [
    "B-DOSAGE", "I-DOSAGE",
    "B-FREQUENCY", "I-FREQUENCY", 
    "B-INSTRUCTION", "I-INSTRUCTION",
    "B-MEDICATION_NAME", "I-MEDICATION_NAME",
    "B-NOTE", "I-NOTE",
    "O"
]

label_to_id = {label: i for i, label in enumerate(label_list)}
id_to_label = {i: label for label, i in label_to_id.items()}
num_labels = len(label_list)

# === 4. Improved Tokenization with Better Handling ===
def tokenize_and_align_labels_improved(example, max_length=256):
    """Enhanced tokenization with better handling of noisy inputs"""
    tokenized_inputs = tokenizer(
        example["tokens"], 
        is_split_into_words=True, 
        padding='max_length', 
        truncation=True,
        max_length=max_length,
        return_overflowing_tokens=False
    )
    
    word_ids = tokenized_inputs.word_ids()
    labels = []
    
    for word_idx in word_ids:
        if word_idx is None:
            labels.append(-100)  # Special tokens
        else:
            label = example["labels"][word_idx]
            labels.append(label_to_id[label])
    
    tokenized_inputs["labels"] = labels
    return tokenized_inputs

# === 5. Enhanced Metrics Computation ===
def compute_metrics_enhanced(p):
    predictions, labels = p
    predictions = predictions.argmax(axis=-1)
    
    true_labels, true_predictions = [], []
    
    for pred, label in zip(predictions, labels):
        filtered_preds, filtered_labels = [], []
        for p_i, l_i in zip(pred, label):
            if l_i != -100:
                filtered_preds.append(id_to_label[p_i])
                filtered_labels.append(id_to_label[l_i])
        true_predictions.append(filtered_preds)
        true_labels.append(filtered_labels)
    
    # Calculate metrics
    metrics = {
        "accuracy": accuracy_score(true_labels, true_predictions),
        "precision": precision_score(true_labels, true_predictions, average='weighted'),
        "recall": recall_score(true_labels, true_predictions, average='weighted'),
        "f1": f1_score(true_labels, true_predictions, average='weighted')
    }
    
    # Add per-class metrics
    try:
        class_report = classification_report(true_labels, true_predictions, output_dict=True)
        for label in label_list:
            if label in class_report:
                metrics[f"{label}_precision"] = class_report[label]["precision"]
                metrics[f"{label}_recall"] = class_report[label]["recall"]
                metrics[f"{label}_f1"] = class_report[label]["f1-score"]
    except:
        pass
    
    return metrics

# === 6. Main Training Function ===
def train_improved_ner_model():
    # Load and preprocess data
    print("Loading and preprocessing data...")
    data = load_and_preprocess_data("labels_for_bert_training.json")
    
    # Apply data augmentation
    print("Applying data augmentation...")
    augmented_data = augment_noisy_data(data, augmentation_factor=1)
    
    # Stratified split
    def get_primary_label(sample):
        for label in sample["labels"]:
            if label != "O":
                return label
        return "O"
    
    labels_for_split = [get_primary_label(sample) for sample in augmented_data]
    train_data, test_data = train_test_split(
        augmented_data, 
        test_size=0.2, 
        stratify=labels_for_split, 
        random_state=42
    )
    
    dataset = DatasetDict({
        "train": Dataset.from_list(train_data),
        "test": Dataset.from_list(test_data)
    })
    
    print(f"Training samples: {len(train_data)}")
    print(f"Test samples: {len(test_data)}")
    
    # Initialize tokenizer
    tokenizer = BertTokenizerFast.from_pretrained("bert-base-cased")
    
    # Tokenize datasets
    print("Tokenizing datasets...")
    tokenized_datasets = dataset.map(
        tokenize_and_align_labels_improved,
        batched=True,
        remove_columns=dataset["train"].column_names
    )
    
    # Initialize model with proper configuration
    model = BertForTokenClassification.from_pretrained(
        "bert-base-cased",
        num_labels=num_labels,
        id2label=id_to_label,
        label2id=label_to_id,
        ignore_mismatched_sizes=True
    )
    
    # Enhanced training arguments
    training_args = TrainingArguments(
        output_dir="./improved_ner_model",
        save_safetensors=True,
        per_device_train_batch_size=4,  # Reduced for better stability
        per_device_eval_batch_size=4,
        num_train_epochs=30,
        evaluation_strategy="epoch",
        save_strategy="epoch",
        logging_dir="./logs_improved",
        logging_strategy="epoch",
        load_best_model_at_end=True,
        metric_for_best_model="eval_f1",
        greater_is_better=True,
        warmup_steps=100,
        weight_decay=0.01,
        learning_rate=3e-5,
        gradient_accumulation_steps=2,
        dataloader_num_workers=2,
        remove_unused_columns=False
    )
    
    # Initialize trainer
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=tokenized_datasets["train"],
        eval_dataset=tokenized_datasets["test"],
        tokenizer=tokenizer,
        compute_metrics=compute_metrics_enhanced,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=5)]
    )
    
    # Train model
    print("Starting training...")
    trainer.train()
    
    # Save model
    model_path = "./improved_ner_medication_model"
    trainer.save_model(model_path)
    tokenizer.save_pretrained(model_path)
    
    # Evaluate
    eval_results = trainer.evaluate()
    print("\nFinal Evaluation Metrics:")
    for k, v in eval_results.items():
        if isinstance(v, float):
            print(f"{k}: {v:.4f}")
        else:
            print(f"{k}: {v}")
    
    return model_path, trainer

# === 7. Enhanced Inference Function ===
def create_enhanced_pipeline(model_path):
    """Create an enhanced NER pipeline with better noise handling"""
    
    # Load model and tokenizer
    tokenizer = BertTokenizerFast.from_pretrained(model_path)
    model = BertForTokenClassification.from_pretrained(model_path)
    
    # Create pipeline with custom post-processing
    ner_pipeline = pipeline(
        "ner", 
        model=model, 
        tokenizer=tokenizer, 
        aggregation_strategy="none"
    )
    
    def enhanced_ner_predict(text, confidence_threshold=0.7):
        """Enhanced NER prediction with noise filtering"""
        
        # Preprocess text to remove excessive noise
        text = re.sub(r'\n+', ' ', text)  # Replace multiple newlines with space
        text = re.sub(r'\s+', ' ', text)  # Normalize whitespace
        text = re.sub(r'[^\w\s\-\.\,\:\;\(\)]', '', text)  # Remove special chars except common ones
        
        # Get NER predictions
        results = ner_pipeline(text)
        
        # Filter by confidence and merge subwords
        filtered_results = []
        current_entity = None
        
        for result in results:
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
                        'score': result['score'],
                        'start': result['start'],
                        'end': result['end']
                    }
        
        if current_entity:
            filtered_results.append(current_entity)
        
        return filtered_results
    
    return enhanced_ner_predict

# === 8. Test Function ===
def test_improved_model(model_path):
    """Test the improved model on noisy input"""
    
    enhanced_predict = create_enhanced_pipeline(model_path)
    
    # Test with your noisy example
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
    
    print("Testing improved model on noisy input...")
    results = enhanced_predict(noisy_text)
    
    print("\nExtracted Entities:")
    for result in results:
        print(f"{result['word']} -> {result['entity']} (confidence: {result['score']:.3f})")
    
    # Group by entity type
    grouped = {}
    for result in results:
        entity_type = result['entity'].replace('B-', '').replace('I-', '')
        if entity_type not in grouped:
            grouped[entity_type] = []
        grouped[entity_type].append(result['word'])
    
    print("\nGrouped Results:")
    for entity_type, words in grouped.items():
        print(f"{entity_type}: {' '.join(words)}")

if __name__ == "__main__":
    # Train the improved model
    model_path, trainer = train_improved_ner_model()
    
    # Test the model
    test_improved_model(model_path)
