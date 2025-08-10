# Dockerfile
FROM python:3.10-slim

# System deps (curl for HEALTHCHECK, tesseract for pytesseract)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates \
    tesseract-ocr \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ENV PYTHONUNBUFFERED=1 \
    HF_HOME=/app/.cache/huggingface

# Install Python deps first (better layer caching)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# --- Pin exact HF model revision into the image (set via build-args in CI) ---
ARG HF_MODEL_ID=prizellia/ner-medimind
ARG HF_REV=main
ARG HF_TOKEN
ENV HF_MODEL_ID=$HF_MODEL_ID HF_REV=$HF_REV HF_TOKEN=$HF_TOKEN

# Download the model snapshot at the given revision into /app/model
RUN python - <<'PY'
from huggingface_hub import snapshot_download
import os
mid = os.environ["HF_MODEL_ID"]
rev = os.environ["HF_REV"]
token = os.environ.get("HF_TOKEN")
snapshot_download(repo_id=mid, revision=rev, local_dir="/app/model", local_dir_use_symlinks=False, token=token)
print("Downloaded model:", mid, "@", rev)
PY

# Copy your app code last (so code edits don't force re-download of the model)
COPY . .

# Healthcheck expects FastAPI to expose GET /health returning 200
HEALTHCHECK --interval=30s --timeout=5s --retries=10 CMD curl -fsS http://localhost:8000/health || exit 1

EXPOSE 8000
CMD ["python","-m","uvicorn","predict:app","--host","0.0.0.0","--port","8000"]
