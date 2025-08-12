#!/bin/bash

# Log Injection Vulnerability Fix Script for MediMind Backend
# This script helps identify and fix log injection vulnerabilities

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Log Injection Vulnerability Scanner${NC}"
echo "============================================="

# Create backup directory
BACKUP_DIR="./backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo -e "${YELLOW}📁 Creating backup in: $BACKUP_DIR${NC}"

# Find all Java files with logging statements
echo -e "${YELLOW}🔍 Scanning for vulnerable logging statements...${NC}"

# Pattern to find logging statements that might be vulnerable
VULNERABLE_PATTERNS=(
    "logger\.(info|warn|error|debug)\(.*\+.*\)"  # String concatenation
    "logger\.(info|warn|error|debug)\(.*\".*\{.*\}.*\".*\)"  # Direct variable logging
    "logger\.(info|warn|error|debug)\(.*toString\(\)"  # toString() calls
    "logger\.(info|warn|error|debug)\(.*getMessage\(\)"  # Exception messages
)

# Files that need manual review
FILES_TO_REVIEW=()

for pattern in "${VULNERABLE_PATTERNS[@]}"; do
    echo -e "${BLUE}Checking pattern: $pattern${NC}"
    files=$(grep -r --include="*.java" -l "$pattern" backend/src/ 2>/dev/null || true)
    
    if [ -n "$files" ]; then
        echo -e "${RED}⚠️  Found potentially vulnerable files:${NC}"
        echo "$files"
        FILES_TO_REVIEW+=($files)
    fi
done

# Specific files that need attention based on our scan
echo -e "${YELLOW}📋 Files requiring log sanitization:${NC}"

# Controller files
CONTROLLER_FILES=(
    "backend/src/main/java/nus/iss/backend/controller/IntakeHistoryController.java"
    "backend/src/main/java/nus/iss/backend/controller/PatientController.java"
    "backend/src/main/java/nus/iss/backend/controller/DoctorController.java"
    "backend/src/main/java/nus/iss/backend/controller/MedicationController.java"
    "backend/src/main/java/nus/iss/backend/controller/WebAuthenticationController.java"
)

# Service files
SERVICE_FILES=(
    "backend/src/main/java/nus/iss/backend/service/Implementation/MedicationImpl.java"
    "backend/src/main/java/nus/iss/backend/service/Implementation/IntakeHistoryImpl.java"
)

echo -e "${BLUE}Controllers to fix:${NC}"
for file in "${CONTROLLER_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ $file (not found)"
    fi
done

echo -e "${BLUE}Services to fix:${NC}"
for file in "${SERVICE_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ $file (not found)"
    fi
done

# Generate fix suggestions
echo -e "${YELLOW}🔧 Fix Suggestions:${NC}"

cat > "$BACKUP_DIR/fix-suggestions.md" << 'EOF'
# Log Injection Fix Suggestions

## Files Already Fixed:
- ✅ PatientServiceImpl.java
- ✅ DoctorImpl.java

## Files Still Need Fixing:

### 1. IntakeHistoryController.java
**Vulnerable lines:**
- Line 42: `logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());`
- Line 46: `logger.error("Error in retrieving log to save doctor notes: " + e.getMessage());`
- Line 64: `logger.error("Patient not found: " + e.getMessage());`
- Line 68: `logger.error("Error retrieving intake history: " + e.getMessage());`
- Line 80: `logger.error(e.getMessage());`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable lines with:
logger.error("Error in retrieving log to save doctor notes: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Patient not found: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error retrieving intake history: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
```

### 2. PatientController.java
**Vulnerable lines:**
- Line 48: `logger.error("Error retrieving patient details by id: " + e.getMessage());`
- Line 64: `logger.error("Error retrieving patient medication: " + e.getMessage());`
- Line 112: `logger.error("Error registering patient: " + e.getMessage(), e);`
- Line 131: `logger.error("Error during patient login: " + e.getMessage(), e);`
- Line 171: `logger.error("Error updating patient: " + e.getMessage(), e);`
- Line 181: `logger.error("Error retrieving patients for doctor MCR {}: {}", mcr, e.getMessage());`
- Line 197: `logger.error("Error unassigning doctor from patient {}: {}", id, e.getMessage());`
- Line 209: `logger.error("Patient not found: " + e.getMessage());`
- Line 212: `logger.error("Error retrieving patient medication: "+ e.getMessage());`
- Line 227: `logger.warn("Assignment failed: " + e.getMessage());`
- Line 230: `logger.error("Error assigning doctor to patient: " + e.getMessage());`
- Line 241: `logger.warn("Invalid request: {}", e.getMessage());`
- Line 244: `logger.error("Unexpected error while fetching unassigned patients: {}", e.getMessage());`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable lines with:
logger.error("Error retrieving patient details by id: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error registering patient: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
logger.error("Error during patient login: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
logger.error("Error updating patient: {}", LogSanitizer.sanitizeForLog(e.getMessage()), e);
logger.error("Error retrieving patients for doctor MCR {}: {}", LogSanitizer.sanitizeForLog(mcr), LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error unassigning doctor from patient {}: {}", id, LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Patient not found: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error retrieving patient medication: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.warn("Assignment failed: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error assigning doctor to patient: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.warn("Invalid request: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Unexpected error while fetching unassigned patients: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
```

### 3. DoctorController.java
**Vulnerable lines:**
- Line 54: `logger.info("Update doctor endpoint called for MCR: " + update.getMcrNo());`
- Line 57: `logger.info("Doctor updated successfully: " + doctor.getMcrNo());`
- Line 60: `logger.error("Doctor not found (Status Code: " + HttpStatus.NOT_FOUND + "): " + e);`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable lines with:
logger.info("Update doctor endpoint called for MCR: {}", LogSanitizer.sanitizeForLog(update.getMcrNo()));
logger.info("Doctor updated successfully: {}", LogSanitizer.sanitizeForLog(doctor.getMcrNo()));
logger.error("Doctor not found (Status Code: {}): {}", HttpStatus.NOT_FOUND, LogSanitizer.sanitizeForLog(e.getMessage()));
```

### 4. MedicationController.java
**Vulnerable lines:**
- Line 132: `logger.info(">>> /save API hit, request received: " + req.toString());`
- Line 134: `logger.info("📦 received notes: " + req.getNotes());`
- Line 142: `logger.info("Note from request: " + med.getNotes());`
- Line 124: `logger.error("Error in retrieving logs for medication(" + medicationId + ").");`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable lines with:
logger.info(">>> /save API hit, request received: {}", LogSanitizer.sanitizeForLog(req.toString()));
logger.info("📦 received notes: {}", LogSanitizer.sanitizeForLog(req.getNotes()));
logger.info("Note from request: {}", LogSanitizer.sanitizeForLog(med.getNotes()));
logger.error("Error in retrieving logs for medication({}).", LogSanitizer.sanitizeForLog(medicationId));
```

### 5. WebAuthenticationController.java
**Vulnerable lines:**
- Line 50: `logger.error("Error authenticating doctor (Status Code: " + HttpStatus.NOT_FOUND + "): " + e);`
- Line 68: `logger.error("Error retrieving session info: " + e.getMessage());`
- Line 86: `logger.error("Logout error: " + e.getMessage());`
- Line 98: `logger.error("Error when registering Doctor: " + e.getMessage());`
- Line 104: `logger.error("Email domain validation failed: " + e.getMessage());`
- Line 120: `logger.error("Error retrieving all clinics: " + e.getMessage());`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable lines with:
logger.error("Error authenticating doctor (Status Code: {}): {}", HttpStatus.NOT_FOUND, LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error retrieving session info: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Logout error: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error when registering Doctor: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Email domain validation failed: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
logger.error("Error retrieving all clinics: {}", LogSanitizer.sanitizeForLog(e.getMessage()));
```

### 6. MedicationImpl.java
**Vulnerable lines:**
- Line 88: `logger.warn("Medication not found!");` (This is safe - static message)
- Line 182: `logger.warn("Patient not found!");` (This is safe - static message)

**Fix:** No fix needed - these are static messages.

### 7. IntakeHistoryImpl.java
**Vulnerable lines:**
- Line 62: `logger.warn("No intake history for medication(" + medicationId + ").");`

**Fix:**
```java
// Add import
import nus.iss.backend.util.LogSanitizer;

// Replace vulnerable line with:
logger.warn("No intake history for medication({}).", LogSanitizer.sanitizeForLog(medicationId));
```

## Summary:
- Total files to fix: 5
- Total vulnerable logging statements: ~25
- Priority: High (security vulnerability)

## Next Steps:
1. Add LogSanitizer import to each file
2. Replace string concatenation with parameterized logging
3. Wrap all user input with LogSanitizer.sanitizeForLog()
4. Test the changes
5. Run SAST scan to verify fixes
EOF

echo -e "${GREEN}✅ Fix suggestions saved to: $BACKUP_DIR/fix-suggestions.md${NC}"

# Count total vulnerable statements
TOTAL_VULNERABLE=$(grep -r --include="*.java" "logger\.(info|warn|error|debug)\(.*\+.*\)" backend/src/ 2>/dev/null | wc -l || echo "0")
TOTAL_DIRECT=$(grep -r --include="*.java" "logger\.(info|warn|error|debug)\(.*\".*\{.*\}.*\".*\)" backend/src/ 2>/dev/null | wc -l || echo "0")

echo ""
echo -e "${BLUE}📊 Summary:${NC}"
echo "  📁 Backup created: $BACKUP_DIR"
echo "  🔍 Total vulnerable statements found: $((TOTAL_VULNERABLE + TOTAL_DIRECT))"
echo "  📄 Fix suggestions: $BACKUP_DIR/fix-suggestions.md"
echo ""
echo -e "${YELLOW}🎯 Next Steps:${NC}"
echo "  1. Review the fix suggestions"
echo "  2. Apply the fixes to each file"
echo "  3. Test your application"
echo "  4. Run SAST scan to verify improvements"
echo ""
echo -e "${GREEN}🎉 Log injection vulnerability scan completed!${NC}"
