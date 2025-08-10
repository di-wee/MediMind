#!/usr/bin/env python3
"""
Test Hugging Face Token and Repository Access
"""

import os
from huggingface_hub import HfApi

def test_hf_access():
    """Test if we can access the HF repository"""
    
    # Check if token is set
    token = os.environ.get("HF_TOKEN")
    if not token:
        print("❌ HF_TOKEN environment variable is not set")
        print("Please set it with: $env:HF_TOKEN='your-token-here'")
        return False
    
    print(f"✅ HF_TOKEN found: {token[:10]}...")
    
    # Test API access
    try:
        api = HfApi(token=token)
        
        # Test repository access
        repo_id = "prizellia/ner-medimind"
        print(f"🔍 Testing access to: {repo_id}")
        
        # Try to list files in the repository
        files = api.list_repo_files(repo_id)
        print(f"✅ Repository accessible! Found {len(files)} files")
        
        # Show some files
        print("📁 Repository contents:")
        for file in files[:5]:  # Show first 5 files
            print(f"  - {file}")
        
        return True
        
    except Exception as e:
        print(f"❌ Error accessing repository: {e}")
        print("Please check:")
        print("1. Your HF token is correct")
        print("2. You have write access to prizellia/ner-medimind")
        print("3. The repository exists")
        return False

if __name__ == "__main__":
    print("🧪 Testing Hugging Face Access")
    print("=" * 40)
    
    if test_hf_access():
        print("\n✅ All tests passed! Ready to push model.")
    else:
        print("\n❌ Please fix the issues above before proceeding.") 