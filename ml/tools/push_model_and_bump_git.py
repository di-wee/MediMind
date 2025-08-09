import os
import subprocess
from huggingface_hub import HfApi

HF_REPO = "prizellia/ner-medimind"  # Your Hugging Face repo
GIT_BRANCH = "ML-Priscilla"         # Deployment branch

# === Prompt for model folder path ===
MODEL_DIR = input("Enter the full path to your trained model folder: ").strip()

if not os.path.exists(MODEL_DIR):
    raise FileNotFoundError(f"Model folder not found: {MODEL_DIR}")

# === Push to Hugging Face ===
api = HfApi(token=os.environ.get("HF_TOKEN"))
if not api.token:
    raise EnvironmentError("HF_TOKEN environment variable is not set. Please set it before running this script.")

res = api.upload_folder(
    repo_id=HF_REPO,
    repo_type="model",
    folder_path=MODEL_DIR
)
hf_sha = res.commit_hash
print(f"Pushed to HF repo '{HF_REPO}', SHA: {hf_sha}")

# === Update model_version.txt ===
with open("model_version.txt", "w") as f:
    f.write(hf_sha + "\n")

# === Commit and push to GitHub ===
subprocess.check_call(["git", "pull", "--rebase"])
subprocess.check_call(["git", "checkout", GIT_BRANCH])
subprocess.check_call(["git", "add", "model_version.txt"])
subprocess.check_call(["git", "commit", "-m", f"chore: bump model to {hf_sha}"])
subprocess.check_call(["git", "push", "origin", GIT_BRANCH])

print(" Done. Git push will trigger CI/CD.")
