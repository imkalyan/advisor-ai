# Advisor-AI

Advisor-AI is a news-driven investment research dashboard. It classifies financial news, aggregates sentiment by ticker, and produces an explainable `BUY`, `HOLD`, or `SELL` signal. It does not place trades.

## How it works

```text
RSS/news feeds → news_events (Kafka) → FinBERT sentiment
    → 7-day aggregation + EWMA by ticker
    → TypeSafe constrained decision: BUY / HOLD / SELL
    → advisor_signals (Kafka) → Spring service → PostgreSQL → React dashboard
```

`news-producer` publishes ticker-linked articles to `news_events`. `sentiment-service` consumes them and runs `ProsusAI/finbert`, producing positive, neutral, and negative probabilities on `news_sentiment`.

`sentiment-aggregator` maintains a seven-day history for each ticker. It calculates average probabilities, numeric sentiment (`positive - negative`), EWMA, sample size, and recent article evidence, then publishes the aggregate to `sentiment_signals`.

`advisor-signals-producer/advisor_signal.py` sends that structured state to TypeSafe's System One endpoint. The request permits exactly three choices: `BUY`, `HOLD`, and `SELL`. TypeSafe must use only the supplied evidence, must not invent facts, and must prefer `HOLD` for sparse, stale, mixed, or uncertain evidence.

The response is validated before publication. It must contain probabilities for all three choices, probabilities summing approximately to one, a finite confidence value, and a selected action matching the highest probability. The resulting signal retains the action, probabilities, confidence, model, source, explanation, sentiment metrics, and supporting articles.

## Fallback behavior

If TypeSafe is unavailable or returns an invalid response, the producer uses a deterministic fallback:

- insufficient sample size → `HOLD`;
- blended sentiment score ≥ `0.4` → `BUY`;
- blended sentiment score ≤ `-0.4` → `SELL`;
- otherwise → `HOLD`.

Fallback signals are labeled `fallback sentiment rule` in `decision_source`.

## How Jev is used

This project uses the same TypeSafe decision pattern as Jev Ultrafast: structured observed state is supplied to a constrained choice, and the model selects from supported options rather than emitting executable code or arbitrary actions.

Advisor-AI does **not** call `jev_ultrafast.Agent` and does not use Jev's browser loop. Stock classification is a server-side event-processing workflow, so it calls TypeSafe directly with structured news and sentiment state. This preserves the Jev-style constrained classification approach without adding an unnecessary browser layer.

## Run locally

Requirements: Docker Desktop and a TypeSafe API key. Keep credentials out of Git; the local `advisor.env` file is ignored.

From this directory:

```bash
docker compose up -d --build
```

Open:

- Dashboard: http://localhost:3000
- Advisor API: http://localhost:8080/api/advisor/ping
- Signals: http://localhost:8080/api/advisor/signals

Useful commands:

```bash
docker compose ps
docker compose logs -f advisor_signals_producer
docker compose down
```

Configure the producer with:

```text
TYPESAFE_API_KEY=...
TYPESAFE_API_URL=https://api.typesafe.ai/v1/systemone
TYPESAFE_MODEL=jev-latest
```

These are research signals only, not personalized financial advice or automatic trade instructions.
