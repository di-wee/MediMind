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
- Manage patients' medication lists
- Monitor medication adherence history
- Flagged alerts for non-compliance (ML-based)

---

## 🧠 Machine Learning Models

| Model Type       | Purpose                                      | Technology                   |
| ---------------- | -------------------------------------------- | ---------------------------- |
| CNN Classifier   | Medication image recognition                 | PyTorch (MobileNet/ResNet18) |
| NER + OCR Parser | Extract dosage/frequency/timing              | EasyOCR/Tesseract + spaCy    |
| BERT-based NER   | Named Entity Recognition for medication info | Transformers (HuggingFace)   |

---

## 💻 Tech Stack

**Frontend**:

- Mobile: Android (Kotlin) with Jetpack Compose, Navigation Component, Room Database, WorkManager
- Web: React.js 19 with Vite, Tailwind CSS, React Router DOM, Axios

**Backend**:

- Java 21 with Spring Boot 3.5.3
- Spring Data JPA, Spring Integration, Spring Batch
- MySQL Database (not PostgreSQL as originally planned)
- Embedded Tomcat (Dockerized)

**Authentication**:

- Firebase Authentication (planned but not yet implemented)

**Database**:

- MySQL (AWS RDS) - using MySQL 8 dialect

**Machine Learning**:

- PyTorch, Transformers (HuggingFace), OpenCV, scikit-learn
- FastAPI for ML service endpoints
- Tesseract OCR for text extraction

**Other Services**:

- AWS S3 (Image storage) - planned
- Firebase Cloud Messaging (Push notifications) - planned

**Development Tools**:

- Gradle (Android)
- Maven (Backend)
- Vite (Frontend)
- Docker & Docker Compose
- Ansible for deployment automation

---

## ☁️ Deployment

- **Backend/API/ML**: AWS EC2
- **Database**: AWS RDS (MySQL)
- **Image Uploads**: AWS S3 (planned)
- **Push Notifications**: Firebase Cloud Messaging (planned)
- **Load Balancer**: AWS Application Load Balancer
- **CDN**: CloudFront (planned)

---

## 🏗️ Architecture

The system follows a microservices architecture with:

- **Android App**: Native Kotlin with MVVM pattern, Room database, WorkManager for background tasks
- **Web Dashboard**: React SPA with component-based architecture
- **Backend API**: RESTful Spring Boot services with JPA/Hibernate
- **ML Service**: FastAPI-based inference service with PyTorch models
- **Database**: MySQL with JPA/Hibernate ORM

---

## 👩‍⚕️ Future Enhancements

- Voice-based reminders using CosyVoice API
- Caregiver alerts for non-response
- Personalized ML-based schedule adjustment
- Firebase Authentication integration
- Push notification implementation

---

Thank you for exploring MediMind — built with compassion, code, and a mission to empower both patients and caregivers.
