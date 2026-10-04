from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

app = FastAPI(title="Sentiment Service")

tok = AutoTokenizer.from_pretrained("ProsusAI/finbert")
mdl = AutoModelForSequenceClassification.from_pretrained("ProsusAI/finbert")
mdl.eval()
LABELS = ["negative", "neutral", "positive"]

class Doc(BaseModel):
    id: str
    text: str

@app.post("/analyze")
def analyze(docs: list[Doc]):
    out = []
    for d in docs:
        inputs = tok(d.text, return_tensors="pt", truncation=True, max_length=512)
        with torch.no_grad():
            logits = mdl(**inputs).logits
            probs = torch.softmax(logits, dim=-1)[0].tolist()
        sentiment = probs[2] - probs[0]  # pos - neg ∈ [-1,1]
        out.append({"id": d.id, "sentiment": sentiment,
                    "probs": dict(zip(LABELS, [float(x) for x in probs]))})
    return {"results": out}