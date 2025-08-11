#!/bin/bash

# Test script to verify backend is running properly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Testing Backend Connectivity${NC}"
echo "====================================="

# Check if backend is running on port 8080
echo -e "${YELLOW}Checking if backend is running on port 8080...${NC}"

# Try multiple endpoints
endpoints=(
    "http://localhost:8080/actuator/health"
    "http://localhost:8080/api/doctor/test"
    "http://localhost:8080/"
    "http://localhost:8080/api"
)

backend_running=false

for endpoint in "${endpoints[@]}"; do
    echo -e "${YELLOW}Trying endpoint: $endpoint${NC}"
    if curl -s --max-time 5 "$endpoint" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend is accessible at: $endpoint${NC}"
        backend_running=true
        break
    else
        echo -e "${RED}❌ Endpoint not accessible: $endpoint${NC}"
    fi
done

if [ "$backend_running" = false ]; then
    echo -e "${RED}❌ Backend is not running on port 8080${NC}"
    echo -e "${YELLOW}Checking what's running on port 8080:${NC}"
    netstat -tlnp | grep 8080 || echo "Nothing running on port 8080"
    
    echo -e "${YELLOW}Checking Java processes:${NC}"
    ps aux | grep java || echo "No Java processes found"
    
    echo -e "${YELLOW}To start the backend:${NC}"
    echo "1. cd backend"
    echo "2. mvn spring-boot:run"
    echo "3. Or set environment variables and run:"
    echo "   export DB_HOST=localhost"
    echo "   export DB_PORT=3306"
    echo "   export DB_NAME=medimind"
    echo "   export DB_USERNAME=root"
    echo "   export DB_PASSWORD=password"
    echo "   mvn spring-boot:run"
    
    exit 1
else
    echo -e "${GREEN}🎉 Backend is running successfully!${NC}"
    exit 0
fi
