# MediMind: Smart Medication Reminder

## 🩺 Project Overview

**MediMind** is a smart medication reminder platform aimed at improving medication adherence for elderly and chronic patients in Singapore. By integrating mobile and web technologies with machine learning, MediMind offers personalized medication reminders, visual recognition of medications, and real-time oversight for caregivers and doctors.

---

## 🚨 Problem Statement

As Singapore moves towards a super-aged society, chronic illnesses such as diabetes, hypertension, and cardiovascular disease require complex medication regimens. However, non-adherence due to forgetfulness, side effects, and low health literacy remains a critical issue, often placing undue stress on caregivers. Existing reminder apps are too complex and fail to contextualize the importance of adherence or track intake history. MediMind addresses this gap with a user-friendly, intelligent, and caregiver-integrated solution.

---

## 📱 Features

### For Patients (Android Mobile App)
- Register/Login with Firebase Authentication
- Upload medication photo for automatic recognition (CNN + OCR/NER)
- Receive contextual education about medications
- Set and receive scheduled medication reminders
- Confirm and track intake logs
- Edit or delete reminders
- View active/inactive medication list
- Missed dose alerts (stretch goal)
- Personalized reminder optimization (stretch goal)
- Feedback system (stretch goal)

### For Doctors (Web App)
- Register/Login with Firebase Authentication
- Manage assigned patients
- View patient profiles and diagnosis
- Manage patients’ medication lists
- Monitor medication adherence history
- Flagged alerts for non-compliance (ML-based)

---

## 🧠 Machine Learning Models

| Model Type         | Purpose                                | Technology              |
|--------------------|----------------------------------------|--------------------------|
| CNN Classifier     | Medication image recognition           | PyTorch (MobileNet/ResNet18) |
| NER + OCR Parser   | Extract dosage/frequency/timing        | EasyOCR/Tesseract + spaCy |


---

## 💻 Tech Stack

**Frontend**:  
- Mobile: Android (Kotlin)  
- Web: React.js (HTML, CSS, JS)

**Backend**:  
- Java Spring Boot  
- Embedded Tomcat (Dockerized)  

**Authentication**:  
- Firebase Authentication

**Database**:  
- PostgreSQL (AWS RDS)

**Machine Learning**:  
- PyTorch, EasyOCR, spaCy

**Other Services**:  
- AWS S3 (Image storage)  
- Firebase Cloud Messaging (Push notifications)

---

## ☁️ Deployment

- **Backend/API/ML**: AWS EC2  
- **Database**: AWS RDS (PostgreSQL)  
- **Image Uploads**: AWS S3  
- **Push Notifications**: Firebase Cloud Messaging


---

## 👩‍⚕️ Future Enhancements
- Voice-based reminders using CosyVoice API
- Caregiver alerts for non-response
- Personalized ML-based schedule adjustment

---

## 🔗 Relationships
- 1 Doctor : Many Patients  
- 1 Patient : 1 Doctor  

---

Thank you for exploring MediMind — built with compassion, code, and a mission to empower both patients and caregivers.
