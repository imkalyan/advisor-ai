import subprocess
from fastapi import FastAPI
from pydantic import BaseModel
import logging

app = FastAPI()
logging.basicConfig(level=logging.INFO)

class SummarizeRequest(BaseModel):
    text: str
    max_tokens: int = 200

def query_ollama_cli(prompt: str, model: str = "llama2:7b") -> str:
    try:
        logging.info(f"Running Ollama CLI for model {model}...")
        result = subprocess.run(
            ["ollama", "run", "--no-stream", model],
            input=prompt.encode(),
            capture_output=True,
            text=True)
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        logging.error(f"Ollama CLI failed: {e.stderr}")
        return f"Error: {e.stderr}"

@app.post("/summarize")
def summarize(request: SummarizeRequest):
    logging.info(f"Received summarize request with text length {len(request.text)}")
    summary = query_ollama_cli(request.text)
    logging.info(f"Ollama CLI summary: {summary[:200]}")
    return {"summary": summary}