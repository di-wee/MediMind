#!/bin/bash

# EC2 Instance Status Check Script
# This script helps you determine if your EC2 instance is done rebooting

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

# Check if environment variables are set
if [ -z "$EC2APP_IP" ]; then
    print_error "EC2APP_IP environment variable not set"
    print_status "Please set it with: export EC2APP_IP='your-ec2-ip'"
    exit 1
fi

if [ -z "$EC2APP_USER" ]; then
    export EC2APP_USER="ubuntu"
    print_warning "EC2APP_USER not set, using default: ubuntu"
fi

print_status "Checking EC2 instance status for IP: $EC2APP_IP"

# Method 1: Check if SSH is accessible
print_status "Method 1: Checking SSH connectivity..."
if ssh -o ConnectTimeout=10 -o BatchMode=yes -o StrictHostKeyChecking=no "$EC2APP_USER@$EC2APP_IP" "echo 'SSH is working'" 2>/dev/null; then
    print_success "SSH is accessible - instance is likely ready"
    SSH_READY=true
else
    print_warning "SSH not accessible yet - instance may still be rebooting"
    SSH_READY=false
fi

# Method 2: Check if services are running (if SSH is available)
if [ "$SSH_READY" = true ]; then
    print_status "Method 2: Checking service status..."
    
    # Check Docker service
    if ssh -o BatchMode=yes -o StrictHostKeyChecking=no "$EC2APP_USER@$EC2APP_IP" "systemctl is-active docker" 2>/dev/null | grep -q "active"; then
        print_success "Docker service is running"
    else
        print_warning "Docker service is not running"
    fi
    
    # Check if containers are running
    CONTAINERS=$(ssh -o BatchMode=yes -o StrictHostKeyChecking=no "$EC2APP_USER@$EC2APP_IP" "docker ps --format 'table {{.Names}}\t{{.Status}}'" 2>/dev/null)
    if [ $? -eq 0 ] && [ -n "$CONTAINERS" ]; then
        print_success "Docker containers are running:"
        echo "$CONTAINERS"
    else
        print_warning "No Docker containers are running"
    fi
    
    # Check if ports are accessible
    print_status "Method 3: Checking port accessibility..."
    
    # Check backend port (8080)
    if nc -z -w5 "$EC2APP_IP" 8080 2>/dev/null; then
        print_success "Backend port 8080 is accessible"
    else
        print_warning "Backend port 8080 is not accessible"
    fi
    
    # Check frontend port (5173)
    if nc -z -w5 "$EC2APP_IP" 5173 2>/dev/null; then
        print_success "Frontend port 5173 is accessible"
    else
        print_warning "Frontend port 5173 is not accessible"
    fi
fi

# Method 4: Check AWS CLI (if available)
if command -v aws &> /dev/null; then
    print_status "Method 4: Checking AWS EC2 instance status..."
    
    # Try to get instance status (you'll need to know the instance ID)
    if [ -n "$EC2_INSTANCE_ID" ]; then
        INSTANCE_STATUS=$(aws ec2 describe-instance-status --instance-ids "$EC2_INSTANCE_ID" --query 'InstanceStatuses[0].InstanceStatus.Status' --output text 2>/dev/null)
        if [ $? -eq 0 ] && [ "$INSTANCE_STATUS" != "None" ]; then
            print_success "AWS reports instance status: $INSTANCE_STATUS"
        else
            print_warning "Could not retrieve AWS instance status"
        fi
    else
        print_warning "EC2_INSTANCE_ID not set - skipping AWS status check"
        print_status "Set it with: export EC2_INSTANCE_ID='your-instance-id'"
    fi
else
    print_warning "AWS CLI not installed - skipping AWS status check"
fi

# Method 5: Check application health endpoints
if [ "$SSH_READY" = true ]; then
    print_status "Method 5: Checking application health..."
    
    # Check backend health
    if curl -s -f "http://$EC2APP_IP:8080/" > /dev/null 2>&1; then
        print_success "Backend is responding"
    else
        print_warning "Backend is not responding"
    fi
    
    # Check frontend health
    if curl -s -f "http://$EC2APP_IP:5173/" > /dev/null 2>&1; then
        print_success "Frontend is responding"
    else
        print_warning "Frontend is not responding"
    fi
fi

echo ""
print_status "Summary:"
if [ "$SSH_READY" = true ]; then
    print_success "✅ EC2 instance appears to be fully booted and ready"
    print_status "You can now access your application at:"
    print_status "  Frontend: http://$EC2APP_IP:5173"
    print_status "  Backend:  http://$EC2APP_IP:8080"
else
    print_warning "⚠️  EC2 instance may still be rebooting"
    print_status "Wait a few more minutes and run this script again"
fi

echo ""
print_status "To run this script again:"
echo "  ./scripts/check-ec2-status.sh"
