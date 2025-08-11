# 🧠 Medication Named Entity Recognition (NER) – BERT Model

This project contains a fine-tuned **BERT-based model** for extracting structured information from medical instruction text.

It recognizes:
- 💊 Medication names
- 📏 Dosage
- 🕒 Frequency
- 📝 Instructions
- 📌 Notes

---

## 📁 Folder Structure

```
bert_ner_model/
├── model/                   # Saved BERT model and tokenizer
├── label2id.json            # Label to index mapping
├── predict.py               # Script for running inference
├── requirements.txt         # Python dependencies
├── training_config/
│   ├── train.py             # Training script
│   └── config.yaml          # Config for training
└── README.md                # This file
```

---

## 🚀 Setup Instructions

```bash
# Create virtual environment (optional)
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

---

## 🏋️‍♂️ Train the Model

Update training parameters in `training_config/config.yaml` and run:

```bash
python training_config/train.py --config training_config/config.yaml
```

This will:
- Load your training data
- Tokenize and align labels
- Train BERT for token classification
- Save the best model to `./model/`

---

## 🔍 Run Inference

```bash
python predict.py "Take 2 tablets of Augmentin 3 times a day after food"
```

Example output:
```json
{
  "medication_name": "Augmentin",
  "dosage": "2",
  "frequency": "3 times a day",
  "instruction": "after food"
}
```

---

## 🗂 Labels

NER labels used:
```
B-DOSAGE, I-DOSAGE
B-FREQUENCY, I-FREQUENCY
B-INSTRUCTION, I-INSTRUCTION
B-MEDICATION_NAME, I-MEDICATION_NAME
B-NOTE, I-NOTE
O
```

---

## 📦 Requirements

- `transformers`
- `torch`
- `datasets`
- `seqeval`
- `scikit-learn`
- `matplotlib`
- `pandas`
- `pyyaml`

---

## 📜 License

MIT License (or modify as needed)