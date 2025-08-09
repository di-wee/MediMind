# MediMind Ansible Deployment

This directory contains the Ansible playbooks and configuration for deploying the MediMind application via CI/CD.

## 📁 File Structure

### Core Files

- **`deploy.yml`** - Main deployment playbook for the MediMind application
- **`inventory.py`** - Dynamic inventory script that reads from environment variables
- **`verify-app.yml`** - Playbook to verify deployment status and health checks

### Templates

- **`templates/docker-compose.yml.j2`** - Docker Compose configuration template
- **`templates/docker-compose.service.j2`** - Systemd service for Docker Compose

### Configuration

- **`ansible.cfg`** - Ansible configuration
- **`README.md`** - This documentation

### CI/CD

- **`.github/workflows/deploy.yml`** - GitHub Actions workflow for automated deployment

## 🚀 CI/CD Deployment

The deployment is fully automated through GitHub Actions:

### Automatic Triggers

- **Push to main branch** - Automatically deploys when changes are pushed
- **Manual trigger** - Can be triggered manually from GitHub Actions UI

### Environment Variables

The workflow uses these GitHub secrets:

- `EC2APP_IP` - Your EC2 instance IP address
- `EC2APP_USER` - SSH username (default: ubuntu)
- `EC2APP_KEY` - SSH private key content
- `DOCKERHUB_USERNAME` - Your Docker Hub username
- `DB_USERNAME`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, `DB_NAME` - Database credentials

### Deployment Process

1. **Checkout code** from repository
2. **Install Ansible** and required collections
3. **Setup SSH key** for EC2 access
4. **Deploy application** using Ansible playbook
5. **Verify deployment** and health checks

## 🔧 Manual Deployment (if needed)

For manual deployment, you can run:

```bash
# Set environment variables
export EC2APP_IP="your-ec2-ip"
export EC2APP_USER="ubuntu"
export DOCKERHUB_USERNAME="your-dockerhub-username"
export DB_USERNAME="your-db-username"
export DB_PASSWORD="your-db-password"
export DB_HOST="your-db-host"
export DB_PORT="your-db-port"
export DB_NAME="your-db-name"

# Deploy
ansible-playbook deploy.yml -i inventory.py

# Verify
ansible-playbook verify-app.yml -i inventory.py
```

## 📋 Application URLs

After successful deployment:

- **Frontend**: http://YOUR_EC2_IP:5173
- **Backend API**: http://YOUR_EC2_IP:8080/api/

## ✅ Features

- **Dynamic inventory** - No hardcoded IP addresses
- **Environment-based configuration** - Uses GitHub secrets
- **Automatic container cleanup** - Prevents deployment conflicts
- **Health checks** - Verifies deployment success
- **Systemd service** - Automatic restart on failure
- **Frontend binding fix** - Properly configured for external access
