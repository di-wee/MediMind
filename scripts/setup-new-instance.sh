#!/bin/bash

# Setup New EC2 Instance Script
# This script helps configure a new EC2 instance for MediMind deployment

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

echo "🚀 MediMind EC2 Instance Setup"
echo "=============================="

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed"
        print_status "Install it with: sudo apt-get install awscli (Ubuntu) or brew install awscli (macOS)"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed"
        print_status "Install it with: sudo apt-get install jq (Ubuntu) or brew install jq (macOS)"
        exit 1
    fi
    
    print_success "All requirements met"
}

# Get instance information
get_instance_info() {
    print_status "Getting instance information..."
    
    # List running instances
    INSTANCES=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" --query 'Reservations[].Instances[]' --output json)
    
    if [ "$(echo $INSTANCES | jq length)" -eq 0 ]; then
        print_error "No running instances found"
        exit 1
    fi
    
    # If multiple instances, let user choose
    if [ "$(echo $INSTANCES | jq length)" -gt 1 ]; then
        print_status "Multiple instances found. Please select one:"
        echo "$INSTANCES" | jq -r '.[] | "\(.InstanceId) - \(.PublicIpAddress // "No Public IP") - \(.InstanceType)"'
        echo ""
        read -p "Enter Instance ID: " INSTANCE_ID
    else
        INSTANCE_ID=$(echo $INSTANCES | jq -r '.[0].InstanceId')
    fi
    
    # Get instance details
    INSTANCE_INFO=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0]' --output json)
    
    PUBLIC_IP=$(echo $INSTANCE_INFO | jq -r '.PublicIpAddress // "N/A"')
    PRIVATE_IP=$(echo $INSTANCE_INFO | jq -r '.PrivateIpAddress // "N/A"')
    INSTANCE_TYPE=$(echo $INSTANCE_INFO | jq -r '.InstanceType // "N/A"')
    KEY_NAME=$(echo $INSTANCE_INFO | jq -r '.KeyName // "N/A"')
    SECURITY_GROUPS=$(echo $INSTANCE_INFO | jq -r '.SecurityGroups[].GroupId // "N/A"')
    
    print_success "Selected instance: $INSTANCE_ID"
    echo "  - Instance Type: $INSTANCE_TYPE"
    echo "  - Public IP: $PUBLIC_IP"
    echo "  - Private IP: $PRIVATE_IP"
    echo "  - Key Name: $KEY_NAME"
    echo "  - Security Groups: $SECURITY_GROUPS"
}

# Configure SSH key
setup_ssh_key() {
    print_status "Setting up SSH key..."
    
    # Check if PEM file exists
    PEM_FILES=()
    for file in ~/.ssh/*.pem; do
        if [ -f "$file" ]; then
            PEM_FILES+=("$file")
        fi
    done
    
    if [ ${#PEM_FILES[@]} -eq 0 ]; then
        print_error "No .pem files found in ~/.ssh/"
        print_status "Please place your new PEM key in ~/.ssh/ and run this script again"
        exit 1
    fi
    
    # If multiple PEM files, let user choose
    if [ ${#PEM_FILES[@]} -gt 1 ]; then
        print_status "Multiple PEM files found. Please select one:"
        for i in "${!PEM_FILES[@]}"; do
            echo "$((i+1)). ${PEM_FILES[$i]}"
        done
        echo ""
        read -p "Enter number (1-${#PEM_FILES[@]}): " choice
        PEM_FILE="${PEM_FILES[$((choice-1))]}"
    else
        PEM_FILE="${PEM_FILES[0]}"
    fi
    
    # Set correct permissions
    chmod 400 "$PEM_FILE"
    print_success "Using SSH key: $PEM_FILE"
}

# Test SSH connection
test_ssh_connection() {
    print_status "Testing SSH connection..."
    
    if [ "$PUBLIC_IP" == "N/A" ] || [ "$PUBLIC_IP" == "null" ]; then
        print_error "Instance has no public IP address"
        print_status "You may need to allocate an Elastic IP"
        return 1
    fi
    
    if timeout 10 ssh -i "$PEM_FILE" -o StrictHostKeyChecking=no -o ConnectTimeout=5 -o BatchMode=yes ubuntu@$PUBLIC_IP "echo 'SSH connection successful'" 2>/dev/null; then
        print_success "SSH connection successful!"
        return 0
    else
        print_error "SSH connection failed"
        print_status "Please check:"
        print_status "  1. Security group allows SSH (port 22)"
        print_status "  2. Instance is fully booted"
        print_status "  3. PEM key is correct"
        return 1
    fi
}

# Configure security groups
configure_security_groups() {
    print_status "Configuring security groups..."
    
    for SG in $SECURITY_GROUPS; do
        echo "  Checking security group: $SG"
        
        # Check for SSH rule
        SSH_RULE=$(aws ec2 describe-security-groups --group-ids $SG --query 'SecurityGroups[0].IpPermissions[?FromPort==`22` && ToPort==`22`].IpRanges[].CidrIp' --output text 2>/dev/null)
        
        if [ -z "$SSH_RULE" ] || [ "$SSH_RULE" == "None" ]; then
            print_warning "SSH rule not found, adding..."
            if aws ec2 authorize-security-group-ingress \
                --group-id $SG \
                --protocol tcp \
                --port 22 \
                --cidr 0.0.0.0/0 2>/dev/null; then
                print_success "SSH rule added to $SG"
            else
                print_error "Failed to add SSH rule to $SG"
            fi
        else
            print_success "SSH rule already exists: $SSH_RULE"
        fi
        
        # Check for application ports
        APP_PORTS=(8080 5173)
        for PORT in "${APP_PORTS[@]}"; do
            PORT_RULE=$(aws ec2 describe-security-groups --group-ids $SG --query "SecurityGroups[0].IpPermissions[?FromPort==\`$PORT\` && ToPort==\`$PORT\`].IpRanges[].CidrIp" --output text 2>/dev/null)
            
            if [ -z "$PORT_RULE" ] || [ "$PORT_RULE" == "None" ]; then
                print_warning "Port $PORT rule not found, adding..."
                if aws ec2 authorize-security-group-ingress \
                    --group-id $SG \
                    --protocol tcp \
                    --port $PORT \
                    --cidr 0.0.0.0/0 2>/dev/null; then
                    print_success "Port $PORT rule added to $SG"
                else
                    print_error "Failed to add port $PORT rule to $SG"
                fi
            else
                print_success "Port $PORT rule already exists: $PORT_RULE"
            fi
        done
    done
}

# Update environment variables
update_env_vars() {
    print_status "Updating environment variables..."
    
    # Create or update .env file
    ENV_FILE=".env"
    cat > $ENV_FILE << EOF
# EC2 Instance Configuration
EC2APP_IP=$PUBLIC_IP
EC2APP_USER=ubuntu
EC2APP_KEY_PATH=$PEM_FILE

# Application URLs
FRONTEND_URL=http://$PUBLIC_IP:5173
BACKEND_URL=http://$PUBLIC_IP:8080

# Database Configuration (update these with your actual values)
DB_USERNAME=admin
DB_PASSWORD=medimind2025
DB_HOST=localhost
DB_PORT=3306
DB_NAME=medimind

# Docker Hub Configuration (update these with your actual values)
DOCKERHUB_USERNAME=your-dockerhub-username
EOF
    
    print_success "Environment variables saved to $ENV_FILE"
    print_status "Please update the database and Docker Hub credentials in $ENV_FILE"
}

# Update Android configuration
update_android_config() {
    print_status "Updating Android configuration..."
    
    # Update ApiClient.kt with new EC2 IP
    API_CLIENT_FILE="android/app/src/main/java/com/example/medimind/network/ApiClient.kt"
    
    if [ -f "$API_CLIENT_FILE" ]; then
        # Create backup
        cp "$API_CLIENT_FILE" "${API_CLIENT_FILE}.backup"
        
        # Update the DEVICE_URL with new EC2 IP
        sed -i.bak "s|private const val DEVICE_URL = \"http://[0-9.]*:8080\"|private const val DEVICE_URL = \"http://$PUBLIC_IP:8080\"|" "$API_CLIENT_FILE"
        
        print_success "Updated Android ApiClient.kt with new EC2 IP: $PUBLIC_IP"
    else
        print_warning "ApiClient.kt not found, skipping Android configuration update"
    fi
}

# Display next steps
show_next_steps() {
    echo ""
    print_status "Setup Complete! Next Steps:"
    echo "================================"
    echo ""
    echo "1. 📝 Update GitHub Secrets (if using CI/CD):"
    echo "   - Go to your GitHub repository → Settings → Secrets and variables → Actions"
    echo "   - Update these secrets:"
    echo "     * EC2APP_IP: $PUBLIC_IP"
    echo "     * EC2APP_KEY: (content of $PEM_FILE)"
    echo "     * DOCKERHUB_USERNAME: (your Docker Hub username)"
    echo "     * DB_USERNAME, DB_PASSWORD, DB_HOST, DB_PORT, DB_NAME"
    echo ""
    echo "2. 🚀 Deploy the application:"
    echo "   - Manual deployment:"
    echo "     export EC2APP_IP=$PUBLIC_IP"
    echo "     export EC2APP_USER=ubuntu"
    echo "     cd ansible && ansible-playbook deploy.yml -i inventory.py"
    echo ""
    echo "   - Or trigger GitHub Actions deployment"
    echo ""
    echo "3. 🔍 Verify deployment:"
    echo "   - Frontend: http://$PUBLIC_IP:5173"
    echo "   - Backend: http://$PUBLIC_IP:8080"
    echo ""
    echo "4. 📱 Update Android app:"
    echo "   - The Android configuration has been updated with the new EC2 IP"
    echo "   - Rebuild and deploy the Android app"
    echo ""
    echo "5. 🔐 SSH Access:"
    echo "   ssh -i $PEM_FILE ubuntu@$PUBLIC_IP"
    echo ""
}

# Main execution
main() {
    check_requirements
    get_instance_info
    setup_ssh_key
    configure_security_groups
    
    if test_ssh_connection; then
        update_env_vars
        update_android_config
        show_next_steps
    else
        print_error "Cannot proceed without SSH access"
        print_status "Please fix the SSH connection issues and run this script again"
        exit 1
    fi
}

# Run main function
main "$@"
