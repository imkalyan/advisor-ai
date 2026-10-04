import os
import json
import time
from kafka import KafkaConsumer, KafkaProducer
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F
import logging
import datetime

logging.basicConfig(level=logging.INFO)

KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
NEWS_TOPIC = "news_events"
SENTIMENT_TOPIC = "news_sentiment"

# Load the FinBERT checkpoint used by the HTTP sentiment service as well.
# Its tokenizer assets are compatible with the Transformers version in this image.
tokenizer = AutoTokenizer.from_pretrained("ProsusAI/finbert")
model = AutoModelForSequenceClassification.from_pretrained("ProsusAI/finbert")

# Kafka consumer & producer
while True:
    try:
        consumer = KafkaConsumer(
            NEWS_TOPIC,
            bootstrap_servers=[KAFKA_BROKER],
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            auto_offset_reset="earliest",
            enable_auto_commit=True
        )
        logging.info(f"Connected to Kafka broker at {KAFKA_BROKER}, consuming from topic '{NEWS_TOPIC}'")
        break
    except Exception as e:
        logging.error(f"Error connecting to Kafka broker at {KAFKA_BROKER}: {e}")
        time.sleep(5)

while True:
        try:
            producer = KafkaProducer(
                bootstrap_servers=[KAFKA_BROKER],
                value_serializer=lambda v: json.dumps(v).encode("utf-8")
            )
            break
        except Exception as e:
            logging.error(f"Error connecting to Kafka broker at {KAFKA_BROKER}: {e}")
            time.sleep(5)

def predict_sentiment(text):
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True, max_length=256)    
    outputs = model(**inputs)
    probs = F.softmax(outputs.logits, dim=-1)
    labels = ["negative", "neutral", "positive"]
    probs_list = probs.tolist()[0]
    
    # build dict instead of raw list
    probabilities = {label: float(prob) for label, prob in zip(labels, probs_list)}
    
    pred_idx = torch.argmax(probs).item()
    return labels[pred_idx], probabilities

logging.info("Starting sentiment consumer...")
for message in consumer:
    news = message.value
    text = news.get("title", "") + " " + news.get("summary", "")
    sentiment, probabilities = predict_sentiment(text)
    timestamp = datetime.datetime.now(datetime.timezone.utc)

    sentiment_signal = {
        "title": news["title"],
        "link": news["link"],
        "published": news["published"],
        "summary": news.get("summary", ""),
        "seen_articles": news["seen_articles"],
        "symbol": news.get("symbol", "UNKNOWN"),
        "timestamp": timestamp.isoformat(),
        "sentiment": sentiment,
        "probabilities": probabilities,
    }

    # produce to sentiment_signals topic
    producer.send(SENTIMENT_TOPIC, sentiment_signal)
    producer.flush()
    print(f"Processed: {news['title']} -> {sentiment}")
    logging.info(f"Processed: {text} -> {sentiment}")
