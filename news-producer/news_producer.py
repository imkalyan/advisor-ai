import os
import time
import json
import csv
import logging
import feedparser
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
from kafka import KafkaProducer
import requests
import io

logging.basicConfig(level=logging.INFO)

# Environment variables
NASDAQ_CSV_PATH = os.getenv("NASDAQ_CSV_PATH", "./nasdaq-listed.csv")
NSE_CSV_URL = os.getenv(
    "NSE_CSV_URL",
    "https://nsearchives.nseindia.com/content/indices/ind_nifty100list.csv"
)
YAHOO_RSS_BASE_URL = os.getenv(
    "YAHOO_RSS_BASE_URL",
    "https://feeds.finance.yahoo.com/rss/2.0/headline?s={symbol}&region=US&lang=en-US"
)
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
TOPIC = "news_events"

COMPANIES = {}
RSS_FEEDS = {}
last_fetched = {}


def load_nasdaq_companies(csv_path):
    logging.info(f"Loading NASDAQ companies from local CSV: {csv_path}")
    companies = {}
    with open(csv_path, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            symbol = row.get("Symbol", "").strip()
            name = row.get("Security Name", "").strip()
            if not symbol or not name:
                continue
            keywords = [w.lower() for w in name.split()] + [symbol.lower()]
            companies[symbol] = keywords
    logging.info(f"Loaded {len(companies)} NASDAQ companies")
    return companies


def load_nse_companies(csv_url):
    logging.info(f"Fetching NSE companies from {csv_url}")
    headers = {"User-Agent": "Mozilla/5.0"}
    resp = requests.get(csv_url, headers=headers)
    resp.raise_for_status()
    text = resp.text
    f = io.StringIO(text)
    reader = csv.DictReader(f)
    companies = {}
    for row in reader:
        symbol = row.get("Symbol", "").strip()
        name = row.get("Company Name", "").strip()
        if not symbol or not name:
            continue
        keywords = [w.lower() for w in name.split()] + [symbol.lower()]
        companies[symbol] = keywords
    logging.info(f"Loaded {len(companies)} NSE companies")
    return companies


def build_rss_feeds(tickers):
    feeds = {}
    for symbol in tickers:
        feeds[symbol] = [YAHOO_RSS_BASE_URL.format(symbol=symbol)]
    return feeds


def extract_symbol(text):
    text = text.lower()
    for ticker, keywords in COMPANIES.items():
        if any(k in text for k in keywords):
            return ticker
    return "UNKNOWN"


seen_links = set()  # global set to prevent duplicate news
# Per-symbol dictionary to track seen links for each symbol
seen_links_by_symbol = {}



def fetch_and_publish_feed(symbol, feed_url, producer):
    """Fetches feed for a symbol and publishes news items, avoiding duplicates."""
    global last_fetched
    news_count = 0
    try:
        feed = feedparser.parse(feed_url)
        for entry in feed.entries[:5]:
            published = entry.get("published_parsed") or entry.get("updated_parsed")
            if published:
                published_dt = datetime(*published[:6], tzinfo=timezone.utc)
                if last_fetched.get(feed_url) and published_dt <= last_fetched[feed_url]:
                    continue
                last_fetched[feed_url] = max(last_fetched.get(feed_url) or published_dt, published_dt)
            else:
                published_dt = datetime.now(timezone.utc)

            link = entry.link
            # Initialize per-symbol seen links set if not present
            if symbol not in seen_links_by_symbol:
                seen_links_by_symbol[symbol] = set()
            if link in seen_links_by_symbol[symbol]:
                continue  # skip duplicates for this symbol
            seen_links_by_symbol[symbol].add(link)
            seen_links.add(link)

            news_item = {
                "title": entry.title,
                "link": link,
                "published": published_dt.isoformat(),
                "summary": entry.get("summary", ""),
                "source": feed_url,
                "symbol": symbol,
                "seen_articles": list(seen_links_by_symbol.get(symbol, []))
            }
            producer.send(TOPIC, news_item)
            logging.info(f"Produced: {symbol} - {entry.title}")
            news_count += 1
        producer.flush()
    except Exception as e:
        logging.error(f"Error while fetching/publishing feed for {symbol}: {e}")
    return news_count


def get_news_parallel(producer, max_workers=8):
    """Fetch and produce news for all symbols in parallel."""
    tasks = []
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        for symbol, feed_urls in RSS_FEEDS.items():
            for feed_url in feed_urls:
                tasks.append(executor.submit(fetch_and_publish_feed, symbol, feed_url, producer))
        for future in as_completed(tasks):
            # Just to raise exceptions if any
            try:
                _ = future.result()
            except Exception as e:
                logging.error(f"Error in news fetching thread: {e}")


def run():
    global COMPANIES, RSS_FEEDS

    # Load companies
    COMPANIES = load_nasdaq_companies(NASDAQ_CSV_PATH)
    COMPANIES.update(load_nse_companies(NSE_CSV_URL))

    RSS_FEEDS = build_rss_feeds(COMPANIES.keys())
    logging.info(f"Built RSS feeds for {len(RSS_FEEDS)} companies.")

    # Initialize Kafka producer
    producer = None
    while producer is None:
        try:
            producer = KafkaProducer(
                bootstrap_servers=[KAFKA_BROKER],
                value_serializer=lambda v: json.dumps(v).encode("utf-8")
            )
            logging.info(f"Connected to Kafka broker at {KAFKA_BROKER} for producing to topic '{TOPIC}'")
        except Exception as e:
            logging.error(f"Kafka not ready, retrying in 5s: {e}")
            time.sleep(5)

    # Continuous news fetching
    logging.info("Starting news producer loop...")
    while True:
        get_news_parallel(producer)
        time.sleep(60)


if __name__ == "__main__":
    run()