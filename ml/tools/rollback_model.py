#!/usr/bin/env python3
"""
Model Rollback Script
Rolls back to a previous model version on both HF Hub and EC2
"""

import os
import subprocess
import sys
from datetime import datetime

def get_deployment_history():
    """Get list of recent deployments"""
    try:
        result = subprocess.run(
            ["git", "log", "--oneline", "model_version.txt"], 
            capture_output=True, text=True
        )
        return result.stdout.strip().split('\n')
    except Exception as e:
        print(f"Error getting deployment history: {e}")
        return []

def rollback_to_commit(commit_hash):
    """Rollback to specific commit"""
    try:
        # Update model_version.txt to previous version
        subprocess.run(["git", "checkout", commit_hash, "model_version.txt"])
        
        # Commit the rollback
        subprocess.run(["git", "add", "model_version.txt"])
        subprocess.run([
            "git", "commit", 
            "-m", f"rollback: revert to model version {commit_hash[:8]}"
        ])
        
        # Push to trigger CI/CD
        subprocess.run(["git", "push", "origin", "ML-Priscilla"])
        
        print(f"✅ Rollback initiated to commit {commit_hash[:8]}")
        print("🔄 CI/CD pipeline will deploy the previous model version")
        
    except Exception as e:
        print(f"❌ Rollback failed: {e}")

def main():
    print("🔄 MediMind Model Rollback Tool")
    print("=" * 40)
    
    # Show recent deployments
    history = get_deployment_history()
    if not history:
        print("❌ No deployment history found")
        return
    
    print("\n📋 Recent deployments:")
    for i, line in enumerate(history[:10]):  # Show last 10
        print(f"{i+1}. {line}")
    
    # Get user choice
    try:
        choice = int(input("\nSelect deployment to rollback to (1-10): ")) - 1
        if 0 <= choice < len(history):
            commit_hash = history[choice].split()[0]
            print(f"\n🔄 Rolling back to: {commit_hash}")
            
            confirm = input("Are you sure? (y/N): ").lower()
            if confirm == 'y':
                rollback_to_commit(commit_hash)
            else:
                print("❌ Rollback cancelled")
        else:
            print("❌ Invalid choice")
    except ValueError:
        print("❌ Please enter a valid number")

if __name__ == "__main__":
    main() 