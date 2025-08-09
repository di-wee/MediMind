# MediMind Ansible Deployment

This directory contains the Ansible playbooks and configuration for deploying the MediMind application.

## 📁 File Structure

### Core Files

- **`deploy.yml`** - Main deployment playbook for the MediMind application
- **`inventory.py`** - Dynamic inventory script that reads from environment variables
- **`verify-app.yml`** - Playbook to verify deployment status and health checks

### Environment Setup

- **`set-env.sh`** - Script to set environment variables from GitHub secrets
- **`setup-local.sh`** - Interactive script for local development setup

### Templates

- **`templates/docker-compose.yml.j2`** - Docker Compose configuration template
- **`templates/docker-compose.service.j2`** - Systemd service for Docker Compose

### CI/CD

- **`.github/workflows/deploy.yml`** - GitHub Actions workflow for automated deployment

## 🚀 Quick Start

### Local Development

```bash
# Interactive setup
./setup-local.sh

# Or manual setup
source set-env.sh
ansible-playbook deploy.yml -i inventory.py
```

### GitHub Actions

The deployment will automatically trigger when you push changes to the main branch.

## 🔧 Environment Variables Required

- `EC2APP_IP` - Your EC2 instance IP address
- `EC2APP_USER` - SSH username (default: ubuntu)
- `EC2APP_KEY` - SSH private key content
- `DOCKERHUB_USERNAME` - Your Docker Hub username
- `DB_USERNAME`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, `DB_NAME` - Database credentials

## 📋 Commands

```bash
# Deploy the application
ansible-playbook deploy.yml -i inventory.py

# Verify deployment status
ansible-playbook verify-app.yml -i inventory.py

# Test connection
ansible all -i inventory.py -m ping
```
