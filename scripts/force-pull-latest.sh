#!/bin/bash

# Force Pull Latest Docker Images Script
# This script ensures you're always pulling the latest Docker images

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get Docker Hub username from environment or prompt
DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME:-""}
if [ -z "$DOCKERHUB_USERNAME" ]; then
    read -p "Enter your Docker Hub username: " DOCKERHUB_USERNAME
fi

if [ -z "$DOCKERHUB_USERNAME" ]; then
    print_error "Docker Hub username is required"
    exit 1
fi

print_status "Force pulling latest Docker images for $DOCKERHUB_USERNAME..."

# Stop and remove existing containers
print_status "Stopping existing containers..."
docker compose down --remove-orphans || true
docker rm -f medimind-backend medimind-frontend || true

# Remove existing images to force fresh pull
print_status "Removing existing images..."
docker rmi $DOCKERHUB_USERNAME/medimind-backend:latest 2>/dev/null || true
docker rmi $DOCKERHUB_USERNAME/medimind-frontend:latest 2>/dev/null || true

# Pull latest images (docker pull will always get the latest)
print_status "Pulling latest images..."
docker pull $DOCKERHUB_USERNAME/medimind-backend:latest
docker pull $DOCKERHUB_USERNAME/medimind-frontend:latest

# Show new image digests
print_status "New image digests:"
docker images --digests | grep $DOCKERHUB_USERNAME

# Start services with latest images
print_status "Starting services with latest images..."
docker compose up -d

print_success "Latest Docker images pulled and services started!"
print_status "You can check the status with: docker ps"
