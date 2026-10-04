import json
import os
from collections import defaultdict, deque
from datetime import datetime, timedelta, timezone
from kafka import KafkaConsumer, KafkaProducer
import logging
logging.basicConfig(level=logging.INFO)

class SentimentAggregator:
    def __init__(self):
        self.KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
        self.INPUT_TOPIC = os.getenv("INPUT_TOPIC", "news_sentiment")
        self.OUTPUT_TOPIC = os.getenv("OUTPUT_TOPIC", "sentiment_signals")

        self.SENTIMENT_SCORE = {
            "positive": 1.0,
            "neutral": 0.0,
            "negative": -1.0
        }

        self.ALPHA = 0.3
        self.WINDOW_DAYS = 7
        self.symbol_history = defaultdict(lambda: deque(maxlen=1000))

        self.consumer = self._init_consumer()
        self.producer = self._init_producer()

    def _init_consumer(self):
        while True:
            logging.info("Connecting to Kafka broker...")
            try:
                consumer = KafkaConsumer(
                    self.INPUT_TOPIC,
                    bootstrap_servers=self.KAFKA_BROKER,
                    value_deserializer=lambda v: json.loads(v.decode("utf-8")),
                    auto_offset_reset="earliest",
                    group_id="sentiment-aggregator-group",
                    enable_auto_commit=True,
                )
                logging.info(f"Connected to Kafka broker at {self.KAFKA_BROKER}, consuming from topic '{self.INPUT_TOPIC}'")
                return consumer
            except Exception as e:
                logging.error(f"Error connecting to Kafka broker at {self.KAFKA_BROKER}: {e}")
                import time
                time.sleep(5)

    def _init_producer(self):
        while True:
            logging.info("Connecting to Kafka producer...")
            try:
                producer = KafkaProducer(
                    bootstrap_servers=[self.KAFKA_BROKER],
                    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
                )
                logging.info(f"Connected to Kafka broker at {self.KAFKA_BROKER} for producing to topic '{self.OUTPUT_TOPIC}'")
                return producer
            except Exception as e:
                logging.error(f"Error connecting to Kafka broker at {self.KAFKA_BROKER}: {e}")
                import time
                time.sleep(5)

    def compute_avg_sentiment(self, probabilities):
        """Convert probability dict to numeric weighted average"""
        return sum(self.SENTIMENT_SCORE[k] * v for k, v in probabilities.items())

    def compute_ewma(self, symbol, new_score, ts, probs):
        """Append new event with EWMA to history and return EWMA."""
        history = self.symbol_history[symbol]

        if not history:  # first entry
            ewma = new_score
        else:
            ewma = self.ALPHA * new_score + (1 - self.ALPHA) * history[-1]["ewma"]

        # Store everything together
        history.append({
            "ts": ts,
            "score": new_score,
            "ewma": ewma,
            "probabilities": probs,
        })
        return ewma

    def run(self):
        while True:
            logging.info("Waiting for messages...")
            for msg in self.consumer:
                signal = msg.value
                symbol = signal.get("symbol", "UNKNOWN")
                ts_str = signal.get("timestamp")
                probs = signal.get("probabilities", {})

                if not ts_str or not probs:
                    continue

                try:
                    ts = datetime.fromisoformat(ts_str)
                except Exception:
                    continue

                # Compute numeric score
                avg_sentiment = self.compute_avg_sentiment(probs)

                # Compute EWMA and append event
                ewma_sentiment = self.compute_ewma(symbol, avg_sentiment, ts, probs)

                # Remove old events beyond 7 days
                cutoff = datetime.now(timezone.utc) - timedelta(days=self.WINDOW_DAYS)
                while self.symbol_history[symbol] and self.symbol_history[symbol][0]["ts"] < cutoff:
                    self.symbol_history[symbol].popleft()

                # Aggregate probabilities over window
                total = {"positive": 0.0, "neutral": 0.0, "negative": 0.0}
                count = len(self.symbol_history[symbol])
                for event in self.symbol_history[symbol]:
                    for k in total.keys():
                        total[k] += event["probabilities"].get(k, 0.0)
                avg = {k: (total[k] / count) if count > 0 else 0.0 for k in total}

                link = signal.get("link")
                evidence = {
                    "title": signal.get("title"),
                    "link": link,
                    "published": signal.get("published"),
                    "summary": signal.get("summary", ""),
                    "sentiment": signal.get("sentiment"),
                    "probabilities": probs,
                    "score": avg_sentiment,
                }
                if self.symbol_history[symbol]:
                    self.symbol_history[symbol][-1]["article"] = evidence
                top_articles = [
                    event["article"]
                    for event in reversed(self.symbol_history[symbol])
                    if event.get("article")
                ][:5]

                aggregated = {
                    "symbol": symbol,
                    "window_days": self.WINDOW_DAYS,
                    "avg_sentiment": avg_sentiment,
                    "ewma_sentiment": ewma_sentiment,
                    "aggregated_probabilities": avg,
                    "sample_size": count,
                    "last_updated": datetime.utcnow().isoformat(),
                    "link": link,
                    "top_articles": top_articles
                }

                self.producer.send(self.OUTPUT_TOPIC, aggregated)
                self.producer.flush()
                logging.info(f"[AGGREGATOR] {symbol} → {aggregated}")

if __name__ == "__main__":
    aggregator = SentimentAggregator()
    aggregator.run()
