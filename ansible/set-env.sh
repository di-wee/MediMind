#!/bin/bash

# Script to set environment variables for Ansible deployment
# This script can be used in GitHub Actions or locally

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up environment variables for MediMind deployment...${NC}"

# Function to check if variable is set
check_var() {
    local var_name=$1
    local var_value=$2
    
    if [ -z "$var_value" ]; then
        echo -e "${RED}❌ $var_name is not set${NC}"
        return 1
    else
        echo -e "${GREEN}✅ $var_name is set${NC}"
        return 0
    fi
}

# Set environment variables from GitHub secrets
# In GitHub Actions, these would be available as secrets
# For local testing, you can set them manually or source from a .env file

# EC2 Configuration
export EC2APP_IP="${EC2APP_IP}"
export EC2APP_USER="${EC2APP_USER:-ubuntu}"
export EC2APP_KEY="${EC2APP_KEY}"

# Docker Hub Configuration
export DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME}"
export DOCKERHUB_TOKEN="${DOCKERHUB_TOKEN}"

# Database Configuration
export DB_USERNAME="${DB_USERNAME}"
export DB_PASSWORD="${DB_PASSWORD}"
export DB_HOST="${DB_HOST}"
export DB_PORT="${DB_PORT}"
export DB_NAME="${DB_NAME}"

# Check if required variables are set
echo -e "\n${YELLOW}Checking required environment variables:${NC}"

required_vars=(
    "EC2APP_IP"
    "DOCKERHUB_USERNAME"
    "DB_USERNAME"
    "DB_PASSWORD"
    "DB_HOST"
    "DB_PORT"
    "DB_NAME"
)

missing_vars=0

for var in "${required_vars[@]}"; do
    if ! check_var "$var" "${!var}"; then
        missing_vars=$((missing_vars + 1))
    fi
done

# Check optional variables
echo -e "\n${YELLOW}Checking optional environment variables:${NC}"
check_var "EC2APP_USER" "$EC2APP_USER"
check_var "DOCKERHUB_TOKEN" "$DOCKERHUB_TOKEN"

# Setup SSH key if EC2APP_KEY is provided
if [ -n "$EC2APP_KEY" ]; then
    echo -e "\n${YELLOW}Setting up SSH key...${NC}"
    
    # Create .ssh directory if it doesn't exist
    mkdir -p ~/.ssh
    
    # Write the SSH key to file
    echo "$EC2APP_KEY" > ~/.ssh/ec2app-key.pem
    chmod 600 ~/.ssh/ec2app-key.pem
    
    echo -e "${GREEN}✅ SSH key saved to ~/.ssh/ec2app-key.pem${NC}"
else
    echo -e "${YELLOW}⚠️  EC2APP_KEY not set - you'll need to manually set up SSH key${NC}"
fi

# Summary
echo -e "\n${YELLOW}Environment Setup Summary:${NC}"
if [ $missing_vars -eq 0 ]; then
    echo -e "${GREEN}✅ All required variables are set${NC}"
    echo -e "${GREEN}✅ Ready to run Ansible playbook${NC}"
    echo -e "\n${YELLOW}To run the playbook:${NC}"
    echo -e "ansible-playbook deploy.yml -i inventory.py"
else
    echo -e "${RED}❌ $missing_vars required variables are missing${NC}"
    echo -e "${YELLOW}Please set the missing variables and run this script again${NC}"
    exit 1
fi

# Display current configuration (without sensitive data)
echo -e "\n${YELLOW}Current Configuration:${NC}"
echo "EC2APP_IP: $EC2APP_IP"
echo "EC2APP_USER: $EC2APP_USER"
echo "DOCKERHUB_USERNAME: $DOCKERHUB_USERNAME"
echo "DB_HOST: $DB_HOST"
echo "DB_PORT: $DB_PORT"
echo "DB_NAME: $DB_NAME"
echo "SSH Key: ~/.ssh/ec2app-key.pem"
