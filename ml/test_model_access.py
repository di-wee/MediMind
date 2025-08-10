#!/usr/bin/env python3
"""
Test if the model exists at the specific SHA
"""

import os
from huggingface_hub import HfApi, snapshot_download

def test_model_access():
    """Test if we can access the model at the specific SHA"""
    
    # Get the SHA from model_version.txt
    with open("model_version.txt", "r") as f:
        hf_sha = f.read().strip()
    
    print(f"Testing model access for SHA: {hf_sha}")
    
    # Test API access
    try:
        api = HfApi(token=os.environ.get("HF_TOKEN"))
        
        # Test repository access
        repo_id = "prizellia/ner-medimind"
        print(f"🔍 Testing access to: {repo_id} @ {hf_sha}")
        
        # Try to list files at the specific revision
        files = api.list_repo_files(repo_id, revision=hf_sha)
        print(f"✅ Model accessible at SHA! Found {len(files)} files")
        
        # Show some files
        print("📁 Model contents:")
        for file in files[:5]:  # Show first 5 files
            print(f"  - {file}")
        
        return True
        
    except Exception as e:
        print(f"❌ Error accessing model: {e}")
        print("Please check:")
        print("1. The SHA exists in the repository")
        print("2. You have read access to the model")
        print("3. The model files are complete")
        return False

if __name__ == "__main__":
    print("🧪 Testing Model Access")
    print("=" * 40)
    
    if test_model_access():
        print("\n✅ Model is accessible! Docker build should work.")
    else:
        print("\n❌ Model access failed. Check the issues above.") 