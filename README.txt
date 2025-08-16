# MediMind - Local Development Setup Guide

## Project Overview
MediMind is a comprehensive medication management system with the following components:
- **Backend**: Spring Boot Java application
- **Frontend**: React.js web application  
- **Android App**: Kotlin-based mobile application
- **ML Server**: Python-based machine learning service

## Prerequisites
- Java 21 (for backend)
- Node.js 18+ (for frontend)
- Python 3.8+ (for ML server)
- MySQL 8.0+
- Android Studio (for Android app)
- Git

## 1. Backend Setup

### Environment Variables
Create `.env` file in `backend/` directory:
```env
# Database Configuration
DB_USERNAME=admin
DB_PASSWORD=medimind2025

# RDS Endpoint (AWS RDS)
DB_HOST=medimind.cr8wwikkc13d.ap-southeast-1.rds.amazonaws.com
DB_PORT=3306
DB_NAME=medimind

# Application Configuration
APP_ENV=development
APP_PORT=8080

# CORS Configuration
CORS_ALLOWED_ORIGIN=http://localhost:5173

```

### Database Setup
1. **AWS RDS Configuration**: The project uses AWS RDS MySQL instance
   - Endpoint: `medimind.cr8wwikkc13d.ap-southeast-1.rds.amazonaws.com`
   - Database: `medimind`
   - Username: `admin`
   - Port: `3306`

2. **Local Development**: For local development, you can either:
   - Use the existing AWS RDS instance (recommended for consistency)
   - Or create a local MySQL database and update the .env file:
     ```env
     DB_HOST=localhost
     DB_USERNAME=your_local_username
     DB_PASSWORD=your_local_password
     ```

3. The application will automatically create tables on startup via:
   - JPA DDL auto-generation
   - Spring Session tables via schema.sql
   - Sample data via DatabaseSeeder

### Running the Backend
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

The backend will be available at: http://localhost:8080

## 2. Frontend Setup

### Environment Variables
Create `.env` file in `frontend/` directory:
```env
# API Configuration
VITE_SERVER=http://localhost:8080/

# Development Configuration
VITE_DEV_MODE=true
```

### Running the Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at: http://localhost:5173


```

### Python Dependencies
```bash
cd ml
pip install -r requirements.txt
```

### Running the ML Server
```bash
cd ml
python app.py
```

The ML server will be available at: http://localhost:5000

## 4. Android App Setup

### Environment Variables
Create `local.properties` file in `android/` directory:
```properties
# SDK Configuration
sdk.dir=/path/to/your/Android/Sdk

# API Configuration
API_BASE_URL=http://10.0.2.2:8080/
ML_API_BASE_URL=http://10.0.2.2:5000/
```

### Network Security Configuration
Update `android/app/src/main/res/xml/network_security_config.xml` to include your local IP:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.3</domain>
        <domain includeSubdomains="true">YOUR_LOCAL_IP</domain>
    </domain-config>
</network-security-config>
```

### Running the Android App
1. Open `android/` folder in Android Studio
2. Sync project with Gradle files
3. Connect Android device or start emulator
4. Run the app

## 5. Docker Setup (Alternative)

### Backend Docker
```bash
cd backend
docker build -t medimind-backend .
docker run -p 8080:8080 --env-file .env medimind-backend
```

### Frontend Docker
```bash
cd frontend
docker build -t medimind-frontend .
docker run -p 5173:5173 --env-file .env medimind-frontend
```

## 6. Development Workflow

### Starting All Services
1. Start MySQL database
2. Start Backend: `cd backend && ./mvnw spring-boot:run`
3. Start ML Server: `cd ml && python app.py`
4. Start Frontend: `cd frontend && npm run dev`
5. Run Android app from Android Studio

### Testing the Setup
1. **Backend**: Visit http://localhost:8080/api/doctor/test
2. **Frontend**: Visit http://localhost:5173
3. **ML Server**: Visit http://localhost:5000/health
4. **Android**: Use camera to take medication photo

## 7. Common Issues & Solutions

### CORS Issues
- Ensure CORS_ALLOWED_ORIGIN in backend .env matches frontend URL
- Check that frontend is running on the correct port

### Database Connection Issues
- Verify MySQL is running and accessible
- Check database credentials in .env file
- Ensure database 'medimind' exists

### Android Network Issues
- Use 10.0.2.2 for Android emulator (localhost equivalent)
- Use your computer's local IP for physical device
- Update network security config for your IP

### ML Server Issues
- Ensure all Python dependencies are installed
- Check model files exist in specified path
- Verify port 5000 is not in use

### Image Upload Issues
- Check ML server is running and accessible
- Verify image compression settings in ImageDetailsFragment.kt
- Ensure proper MIME type handling

## 8. Development Tips

### Backend Development
- Enable SQL logging: `spring.jpa.show-sql=true` in application.properties
- Use DatabaseSeeder for sample data
- Check logs for Spring Session table creation

### Frontend Development
- Use browser dev tools for API debugging
- Check network tab for CORS issues
- Use React dev tools for component debugging

### Android Development
- Use Android Studio's logcat for debugging
- Test on both emulator and physical device
- Check network security config for API calls

### ML Server Development
- Monitor server logs for prediction requests
- Test with sample images in ml/test/ directory
- Verify model loading on startup

## 9. File Structure
```
MediMind/
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration files
│   └── .env               # Backend environment variables
├── frontend/               # React.js application
│   ├── src/               # React source code
│   ├── public/            # Static assets
│   └── .env              # Frontend environment variables
├── android/               # Android application
│   ├── app/src/main/     # Kotlin source code
│   └── local.properties  # Android configuration
├── ml/                   # Machine learning server
│   ├── app.py           # ML server application
│   ├── requirements.txt # Python dependencies
│   └── .env            # ML server environment variables
└── README.txt           # This file
```

## 10. Support
For issues or questions:
1. Check the logs of each service
2. Verify all environment variables are set correctly
3. Ensure all prerequisites are installed
4. Check network connectivity between services


