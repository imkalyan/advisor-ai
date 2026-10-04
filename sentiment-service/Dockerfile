FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .
# Install CPU-only torch first (smaller image, works on Apple Silicon)
RUN pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu torch torchvision torchaudio \
 && pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000
CMD ["./wait-for-it.sh", "kafka:9092", "--", "./start.sh"]