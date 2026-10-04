import os
import json
import logging
import math
import requests
from bs4 import BeautifulSoup
from kafka import KafkaConsumer, KafkaProducer
import time

logging.basicConfig(level=logging.INFO)

SENTIMENT_TOPIC = os.getenv("SENTIMENT_TOPIC", "sentiment_signals")
ADVISOR_TOPIC = os.getenv("ADVISOR_TOPIC", "advisor_signals")
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
TYPESAFE_API_URL = os.getenv("TYPESAFE_API_URL", "https://api.typesafe.ai/v1/systemone")
TYPESAFE_MODEL = os.getenv("TYPESAFE_MODEL", "jev-latest")
TYPESAFE_API_KEY = os.getenv("TYPESAFE_API_KEY")
MAX_TOP_ARTICLES = 3
MAX_ARTICLE_CHARS = 6000

while True:
    try:
        producer = KafkaProducer(
            bootstrap_servers=[KAFKA_BROKER],
            value_serializer=lambda v: json.dumps(v).encode("utf-8")
        )
        logging.info(f"Connected to Kafka broker at {KAFKA_BROKER} for producing to topic '{ADVISOR_TOPIC}'")
        break
    except Exception as e:  
        logging.error(f"Error connecting to Kafka broker at {KAFKA_BROKER}: {e}")
        import time
        time.sleep(5)

while True:
    try:        
        consumer = KafkaConsumer(
            SENTIMENT_TOPIC,
            bootstrap_servers=[KAFKA_BROKER],
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            auto_offset_reset="earliest",
            enable_auto_commit=True
        )
        logging.info(f"Connected to Kafka broker at {KAFKA_BROKER}, consuming from topic '{SENTIMENT_TOPIC}'")
        break
    except Exception as e:
        logging.error(f"Error connecting to Kafka broker at {KAFKA_BROKER}: {e}")
        import time
        time.sleep(5)

def determine_action(probabilities, ewma_sentiment=None, sample_size=0, min_sample=1):
    """
    Determine a deterministic fallback action from numeric sentiment, EWMA,
    and sample size.
    """
    if sample_size < min_sample:
        # Default to HOLD for low confidence
        return "HOLD"

    pos = probabilities.get("positive", 0.0)
    neg = probabilities.get("negative", 0.0)
    neu = probabilities.get("neutral", 0.0)

    # Basic numeric sentiment score (-1 to +1)
    numeric_score = pos - neg

    # Incorporate EWMA sentiment if available
    if ewma_sentiment is not None:
        numeric_score = 0.7 * numeric_score + 0.3 * ewma_sentiment

    # Define thresholds for action
    # Make them symmetric, and narrow for strong signals
    if numeric_score >= 0.4:
        return "BUY"
    elif numeric_score <= -0.4:
        return "SELL"
    else:
        return "HOLD"


def _validate_typesafe_choice(answer, ids):
    try:
        probabilities = answer["probabilities"]
        numbers = [*probabilities.values(), answer["confidence"]]
        valid = (
            answer["choice"] in ids
            and set(probabilities) == set(ids)
            and all(type(n) in (int, float) and math.isfinite(n) and 0 <= n <= 1 for n in numbers)
            and abs(sum(probabilities.values()) - 1) < 0.02
            and probabilities[answer["choice"]] >= max(probabilities.values()) - 1e-6
        )
    except (KeyError, TypeError, ValueError):
        valid = False
    if not valid:
        raise ValueError("Invalid TypeSafe advisor response")
    return answer


def call_typesafe_with_retry(payload, retries=3, delay=2):
    """Call TypeSafe with retries for transient provider errors."""
    if not TYPESAFE_API_KEY:
        raise RuntimeError("TYPESAFE_API_KEY is not configured")
    for attempt in range(retries):
        try:
            resp = requests.post(
                TYPESAFE_API_URL,
                json=payload,
                headers={"Authorization": f"Bearer {TYPESAFE_API_KEY}"},
                timeout=30,
            )
            logging.info(f"TypeSafe advisor response status: {resp.status_code}")
            if resp.status_code in {429, 503, 529} and attempt < retries - 1:
                time.sleep(delay * (2 ** attempt))
                continue
            resp.raise_for_status()
            return resp.json()
        except Exception as e:
            if attempt == retries - 1:
                raise
            logging.warning(f"TypeSafe call failed (attempt {attempt+1}/{retries}): {e}")
            time.sleep(delay * (2 ** attempt))


def compact_articles(articles, limit=MAX_TOP_ARTICLES):
    compacted = []
    for article in (articles or [])[:limit]:
        if isinstance(article, dict):
            compacted.append({
                "title": article.get("title", ""),
                "summary": article.get("summary", ""),
                "link": article.get("link", ""),
                "published": article.get("published", ""),
                "sentiment": article.get("sentiment"),
                "probabilities": article.get("probabilities"),
                "score": article.get("score"),
                "article_text": article.get("article_text", ""),
            })
        else:
            compacted.append({"link": str(article)})
    return compacted


def enrich_directional_articles(articles):
    """Fetch bounded article text only for a directional candidate."""
    enriched = []
    for article in (articles or [])[:MAX_TOP_ARTICLES]:
        item = dict(article) if isinstance(article, dict) else {"link": str(article)}
        link = item.get("link")
        if link and not item.get("article_text"):
            try:
                response = requests.get(
                    link,
                    headers={"User-Agent": "Advisor-AI research reader/1.0"},
                    timeout=8,
                )
                response.raise_for_status()
                soup = BeautifulSoup(response.text, "html.parser")
                for node in soup(["script", "style", "noscript"]):
                    node.decompose()
                item["article_text"] = " ".join(soup.get_text(" ").split())[:MAX_ARTICLE_CHARS]
            except requests.RequestException as error:
                logging.warning("Could not fetch article evidence %s: %s", link, error)
        enriched.append(item)
    return enriched


def advisor_reason(action, probabilities, articles, source, error=None):
    top = compact_articles(articles, 2)
    evidence_bits = []
    for article in top:
        title = article.get("title") or article.get("link")
        sentiment = article.get("sentiment")
        if title:
            evidence_bits.append(f"{title}" + (f" ({sentiment})" if sentiment else ""))
    if evidence_bits:
        evidence = "; ".join(evidence_bits)
    else:
        evidence = "no article details were available"
    prob_text = ", ".join(f"{k}={v:.2f}" for k, v in (probabilities or {}).items())
    reason = f"{action} signal from {source}: {prob_text or 'no probabilities'}; evidence: {evidence}."
    if error:
        reason += f" TypeSafe unavailable, used fallback ({error})."
    return reason


def decide_with_typesafe(symbol_data, fallback_action):
    source_articles = symbol_data.get("top_articles", [])
    if fallback_action in {"BUY", "SELL"}:
        source_articles = enrich_directional_articles(source_articles)
    articles = compact_articles(source_articles)
    probabilities = symbol_data.get("aggregated_probabilities", {})
    state = {
        "symbol": symbol_data.get("symbol"),
        "sentiment_metrics": {
            "avg_sentiment": symbol_data.get("avg_sentiment"),
            "ewma_sentiment": symbol_data.get("ewma_sentiment"),
            "aggregated_probabilities": probabilities,
            "sample_size": symbol_data.get("sample_size"),
            "last_updated": symbol_data.get("last_updated"),
        },
        "latest_news_evidence": articles,
        "rule_based_fallback_action": fallback_action,
    }
    criteria = {
        "BUY": (
            "Evidence is materially positive and recent, with supportive sentiment. "
            "Use only when upside evidence clearly outweighs negative or uncertain evidence."
        ),
        "HOLD": (
            "Evidence is mixed, thin, stale, neutral, or not strong enough for BUY or SELL. "
            "Prefer HOLD when sample size is low or confidence is limited."
        ),
        "SELL": (
            "Evidence is materially negative and recent, with adverse sentiment. "
            "Use only when downside evidence clearly outweighs positive or uncertain evidence."
        ),
    }
    payload = {
        "model": TYPESAFE_MODEL,
        "state": state,
        "questions": {
            "action": {
                "type": "choice",
                "criteria": criteria,
                "instructions": {
                    "role": "You are an evidence-ranking system for an investing dashboard, not a personal financial advisor.",
                    "task": "Choose BUY, HOLD, or SELL for the ticker using only the supplied news evidence and sentiment metrics.",
                    "rules": [
                        "Do not invent facts beyond the supplied evidence.",
                        "Prefer HOLD when evidence is insufficient, stale, or mixed.",
                        "Treat the output as a dashboard signal, not personalized financial advice.",
                    ],
                },
            }
        },
    }
    result = call_typesafe_with_retry(payload)
    answer = _validate_typesafe_choice(result["answers"].get("action", {}), criteria)
    return {
        "action": answer["choice"],
        "probabilities": answer["probabilities"],
        "confidence": answer["confidence"],
        "model": result.get("model", TYPESAFE_MODEL),
        "usage": result.get("usage", {}),
        "request": payload,
    }


def process_sentiment_messages():
    logging.info("Starting advisor signal processor with TypeSafe decisions...")
    for message in consumer:
        try:
            symbol_data = message.value
            top_articles = symbol_data.get("top_articles", [])

            fallback_action = determine_action(
                probabilities=symbol_data.get("aggregated_probabilities", {}),
                ewma_sentiment=symbol_data.get("ewma_sentiment"),
                sample_size=symbol_data.get("sample_size", 0),
            )

            decision_source = "TypeSafe"
            typesafe = None
            try:
                typesafe = decide_with_typesafe(symbol_data, fallback_action)
                final_action = typesafe["action"]
                action_probabilities = typesafe["probabilities"]
            except Exception as e:
                logging.error(f"TypeSafe advisor decision failed for {symbol_data.get('symbol')}: {e}")
                decision_source = "fallback sentiment rule"
                final_action = fallback_action
                action_probabilities = {
                    "BUY": 1.0 if final_action == "BUY" else 0.0,
                    "HOLD": 1.0 if final_action == "HOLD" else 0.0,
                    "SELL": 1.0 if final_action == "SELL" else 0.0,
                }
                typesafe = {"error": str(e)}

            reason = advisor_reason(
                final_action,
                action_probabilities,
                (typesafe or {}).get("request", {}).get("state", {}).get("latest_news_evidence", top_articles),
                decision_source,
                error=typesafe.get("error") if typesafe else None,
            )
            evidence_articles = (
                (typesafe or {}).get("request", {})
                .get("state", {})
                .get("latest_news_evidence", top_articles)
            )

            advisor_signal = {
                "symbol": symbol_data.get("symbol"),
                "window_days": symbol_data.get("window_days"),
                "action": final_action,
                "fallback_action": fallback_action,
                "avg_sentiment": symbol_data.get("avg_sentiment"),
                "ewma_sentiment": symbol_data.get("ewma_sentiment"),
                "aggregated_probabilities": symbol_data.get("aggregated_probabilities"),
                "action_probabilities": action_probabilities,
                "sample_size": symbol_data.get("sample_size"),
                "last_updated": symbol_data.get("last_updated"),
                "reason": reason,
                "decision_source": decision_source,
                "decision_model": typesafe.get("model") if typesafe else None,
                "decision_confidence": typesafe.get("confidence") if typesafe else None,
                "decision_usage": typesafe.get("usage") if typesafe else {},
                "top_articles": evidence_articles
            }

            producer.send(ADVISOR_TOPIC, advisor_signal)
            producer.flush()
            logging.info(f"Published enriched advisor signal: {advisor_signal['symbol']} → {final_action} -> {reason}")

        except Exception as e:
            logging.error(f"Error processing sentiment message: {e}")
            continue

if __name__ == "__main__":
    process_sentiment_messages()
