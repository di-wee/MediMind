#!/bin/bash

# Local setup script for MediMind deployment
# This script helps you set environment variables locally

echo "Setting up environment variables for local Ansible deployment..."

# Function to prompt for input with default value
prompt_with_default() {
    local prompt="$1"
    local default="$2"
    local var_name="$3"
    
    if [ -n "$default" ]; then
        read -p "$prompt [$default]: " input
        export "$var_name"="${input:-$default}"
    else
        read -p "$prompt: " input
        export "$var_name"="$input"
    fi
}

echo "Please enter your GitHub secrets values:"
echo ""

# EC2 Configuration
prompt_with_default "EC2 Application IP" "" "EC2APP_IP"
prompt_with_default "EC2 Username" "ubuntu" "EC2APP_USER"

# Docker Hub Configuration
prompt_with_default "Docker Hub Username" "" "DOCKERHUB_USERNAME"

# Database Configuration
prompt_with_default "Database Username" "" "DB_USERNAME"
prompt_with_default "Database Password" "" "DB_PASSWORD"
prompt_with_default "Database Host" "" "DB_HOST"
prompt_with_default "Database Port" "5432" "DB_PORT"
prompt_with_default "Database Name" "" "DB_NAME"

# SSH Key setup
echo ""
echo "For the SSH key, you have two options:"
echo "1. Enter the key content directly (paste the entire key)"
echo "2. Provide the path to an existing key file"
read -p "Choose option (1 or 2): " key_option

if [ "$key_option" = "1" ]; then
    echo "Please paste your SSH private key content (press Enter when done):"
    read -d '' -r ssh_key_content
    echo "$ssh_key_content" > ~/.ssh/id_rsa
    chmod 600 ~/.ssh/id_rsa
    echo "SSH key saved to ~/.ssh/id_rsa"
elif [ "$key_option" = "2" ]; then
    read -p "Enter path to your SSH key file: " key_path
    if [ -f "$key_path" ]; then
        cp "$key_path" ~/.ssh/id_rsa
        chmod 600 ~/.ssh/id_rsa
        echo "SSH key copied to ~/.ssh/id_rsa"
    else
        echo "Error: Key file not found at $key_path"
        exit 1
    fi
else
    echo "Invalid option. Please run the script again."
    exit 1
fi

echo ""
echo "Environment variables set successfully!"
echo ""
echo "To run the Ansible playbook:"
echo "ansible-playbook deploy.yml -i inventory.py"
echo ""
echo "Or to test the setup:"
echo "ansible all -i inventory.py -m ping"
