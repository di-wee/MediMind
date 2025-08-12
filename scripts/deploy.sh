#!/bin/bash

# MediMind Deployment Script with DAST Security Gates
# This script ensures security compliance before deployment

set -e

# Configuration
ENVIRONMENT=${1:-staging}
FRONTEND_URL="http://localhost:3000"
BACKEND_URL="http://localhost:8080"
SECURITY_REPORTS_DIR="./security-reports"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 Starting MediMind Deployment to $ENVIRONMENT${NC}"
echo "=================================================="

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker is not installed${NC}"
        exit 1
    fi
    
    # Check if Docker Compose is available
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}❌ Docker Compose is not available${NC}"
        exit 1
    fi
    
    # Check if required scripts exist
    if [ ! -f "scripts/dast-scan.sh" ]; then
        echo -e "${RED}❌ DAST scan script not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Prerequisites check passed${NC}"
}

# Function to build and start applications
build_and_start_apps() {
    echo -e "${YELLOW}Building and starting applications...${NC}"
    
    # Build and start backend
    echo "Building backend..."
    cd backend
    mvn clean compile
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8080" &
    BACKEND_PID=$!
    cd ..
    
    # Wait for backend to start
    echo "Waiting for backend to start..."
    sleep 30
    
    # Build and start frontend
    echo "Building frontend..."
    cd frontend
    npm ci
    npm run build
    npm run preview -- --port 3000 &
    FRONTEND_PID=$!
    cd ..
    
    # Wait for frontend to start
    echo "Waiting for frontend to start..."
    sleep 15
    
    echo -e "${GREEN}✅ Applications started${NC}"
}

# Function to run DAST security scan
run_security_scan() {
    echo -e "${YELLOW}🔒 Running DAST Security Scan...${NC}"
    
    # Set environment variables for DAST scan
    export FRONTEND_URL="$FRONTEND_URL"
    export BACKEND_URL="$BACKEND_URL"
    export SCAN_TIMEOUT="600"
    
    # Run the DAST scan
    if ./scripts/dast-scan.sh; then
        echo -e "${GREEN}✅ DAST scan completed successfully${NC}"
        return 0
    else
        echo -e "${RED}❌ DAST scan failed or found critical issues${NC}"
        return 1
    fi
}

# Function to run Docker-based DAST scan
run_docker_dast() {
    echo -e "${YELLOW}🔒 Running Docker-based DAST Security Scan...${NC}"
    
    # Set environment variables
    export TARGET_URL="$FRONTEND_URL"
    export API_URL="$BACKEND_URL/v3/api-docs"
    export FRONTEND_URL="$FRONTEND_URL"
    export BACKEND_URL="$BACKEND_URL"
    
    # Create security reports directory
    mkdir -p "$SECURITY_REPORTS_DIR"
    
    # Run Docker Compose DAST scan
    if docker-compose -f docker-compose.dast.yml up --abort-on-container-exit; then
        echo -e "${GREEN}✅ Docker DAST scan completed${NC}"
        
        # Check for critical issues
        if [ -f "$SECURITY_REPORTS_DIR/security_summary.md" ]; then
            echo "Security scan results:"
            cat "$SECURITY_REPORTS_DIR/security_summary.md"
        fi
        
        return 0
    else
        echo -e "${RED}❌ Docker DAST scan failed${NC}"
        return 1
    fi
}

# Function to check security compliance
check_security_compliance() {
    echo -e "${YELLOW}Checking security compliance...${NC}"
    
    # Check for critical issues in reports
    local critical_issues=0
    local high_issues=0
    
    for json_file in "$SECURITY_REPORTS_DIR"/*.json; do
        if [ -f "$json_file" ]; then
            local critical=$(jq -r '.alerts[] | select(.risk == "Critical") | .name' "$json_file" 2>/dev/null | wc -l)
            local high=$(jq -r '.alerts[] | select(.risk == "High") | .name' "$json_file" 2>/dev/null | wc -l)
            
            critical_issues=$((critical_issues + critical))
            high_issues=$((high_issues + high))
        fi
    done
    
    if [ "$critical_issues" -gt 0 ]; then
        echo -e "${RED}❌ CRITICAL: $critical_issues critical security issues found${NC}"
        echo -e "${RED}Deployment is BLOCKED due to security vulnerabilities${NC}"
        return 1
    fi
    
    if [ "$high_issues" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  WARNING: $high_issues high severity issues found${NC}"
        echo -e "${YELLOW}Deployment can proceed but review recommended${NC}"
    fi
    
    echo -e "${GREEN}✅ Security compliance check passed${NC}"
    return 0
}

# Function to deploy to target environment
deploy_to_environment() {
    echo -e "${YELLOW}Deploying to $ENVIRONMENT...${NC}"
    
    case $ENVIRONMENT in
        "staging")
            # Deploy to staging environment
            echo "Deploying to staging environment..."
            # Add your staging deployment commands here
            ;;
        "production")
            # Deploy to production environment
            echo "Deploying to production environment..."
            # Add your production deployment commands here
            ;;
        *)
            echo -e "${RED}Unknown environment: $ENVIRONMENT${NC}"
            exit 1
            ;;
    esac
    
    echo -e "${GREEN}✅ Deployment to $ENVIRONMENT completed${NC}"
}

# Function to cleanup
cleanup() {
    echo -e "${YELLOW}Cleaning up...${NC}"
    
    # Stop applications
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    
    # Stop Docker containers
    docker-compose -f docker-compose.dast.yml down 2>/dev/null || true
    
    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Function to display help
show_help() {
    echo "Usage: $0 [environment]"
    echo ""
    echo "Environments:"
    echo "  staging     Deploy to staging environment"
    echo "  production  Deploy to production environment"
    echo ""
    echo "Examples:"
    echo "  $0 staging"
    echo "  $0 production"
    echo ""
    echo "Security Features:"
    echo "  - DAST scanning before deployment"
    echo "  - Security gates to block deployment on critical issues"
    echo "  - Comprehensive security reporting"
}

# Main execution
main() {
    # Parse command line arguments
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_help
        exit 0
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Build and start applications
    build_and_start_apps
    
    # Run security scan (choose one method)
    echo "Choose DAST scan method:"
    echo "1. Script-based DAST scan"
    echo "2. Docker-based DAST scan"
    read -p "Enter choice (1 or 2): " scan_choice
    
    case $scan_choice in
        1)
            if ! run_security_scan; then
                cleanup
                exit 1
            fi
            ;;
        2)
            if ! run_docker_dast; then
                cleanup
                exit 1
            fi
            ;;
        *)
            echo -e "${RED}Invalid choice${NC}"
            cleanup
            exit 1
            ;;
    esac
    
    # Check security compliance
    if ! check_security_compliance; then
        cleanup
        exit 1
    fi
    
    # Deploy to target environment
    deploy_to_environment
    
    # Cleanup
    cleanup
    
    echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
    echo -e "${BLUE}📁 Security reports available in: $SECURITY_REPORTS_DIR${NC}"
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function
main "$@" 