-- Users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    risk_profile TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Accounts (brokerage, demat, crypto wallet, etc.)
CREATE TABLE IF NOT EXISTS accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    "broker" TEXT,
    "type" TEXT CHECK (type IN ('BROKERAGE', 'MF', 'ETF', 'CRYPTO', 'BOND', 'CASH')),
    masked_id TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
    txn_id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    market TEXT NOT NULL,
    side TEXT NOT NULL,
    quantity NUMERIC NOT NULL,
    price NUMERIC NOT NULL,
    txn_ccy TEXT NOT NULL,
    txn_time TIMESTAMPTZ NOT NULL,
    fees NUMERIC
);

-- Positions
CREATE TABLE IF NOT EXISTS positions (
    position_id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    market TEXT NOT NULL,
    qty NUMERIC NOT NULL,
    avg_cost NUMERIC NOT NULL,
    last_updated TIMESTAMPTZ DEFAULT now()
);

-- Fundamentals
CREATE TABLE IF NOT EXISTS fundamentals (
    symbol TEXT PRIMARY KEY,
    pe NUMERIC,
    pb NUMERIC,
    ps NUMERIC,
    roe NUMERIC,
    debt_to_equity NUMERIC,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- News
CREATE TABLE IF NOT EXISTS news_events (
    id BIGSERIAL PRIMARY KEY,
    source TEXT,
    "url" TEXT UNIQUE,
    published_at TIMESTAMPTZ,
    title TEXT,
    body TEXT,
    lang TEXT
);

-- Article-Ticker mapping
CREATE TABLE IF NOT EXISTS article_tickers (
    article_id BIGINT REFERENCES news_events(id) ON DELETE CASCADE,
    symbol TEXT,
    confidence NUMERIC,
    PRIMARY KEY(article_id, symbol)
);

-- Sentiment signals
CREATE TABLE IF NOT EXISTS sentiment_signals (
    symbol TEXT,
    "window" TEXT,
    ts TIMESTAMPTZ,
    score NUMERIC,
    volume INT,
    surprise NUMERIC,
    PRIMARY KEY(symbol, "window", ts)
);

-- Advisor signals
CREATE TABLE IF NOT EXISTS advisor_signals (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(20),
    window_days INT,
    avg_sentiment DOUBLE PRECISION,
    ewma_sentiment DOUBLE PRECISION,
    sample_size INT,
    last_updated TIMESTAMPTZ DEFAULT now(),
    "action" VARCHAR(10),
    reason TEXT,
    aggregated_probabilities JSONB
);

-- Holdings
CREATE TABLE IF NOT EXISTS holdings (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID REFERENCES accounts(account_id) ON DELETE CASCADE,
    symbol TEXT NOT NULL,
    asset_type TEXT CHECK (asset_type IN ('STOCK', 'MF', 'ETF', 'CRYPTO', 'BOND', 'CASH')),
    quantity NUMERIC(20,6) NOT NULL,
    cost_basis NUMERIC(20,6),
    as_of DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX IF NOT EXISTS idx_holdings_user_asof ON holdings(user_id, as_of);

-- Prices
CREATE TABLE IF NOT EXISTS prices (
    symbol TEXT NOT NULL,
    "date" DATE NOT NULL,
    "close" NUMERIC(20,6) NOT NULL,
    fx NUMERIC(20,6),
    source TEXT,
    PRIMARY KEY (symbol, date)
);

-- Benchmarks
CREATE TABLE IF NOT EXISTS benchmarks (
    code TEXT NOT NULL,
    "date" DATE NOT NULL,
    "value" NUMERIC(20,6) NOT NULL,
    PRIMARY KEY (code, date)
);

-- Risk profile per user
CREATE TABLE IF NOT EXISTS risk_profile (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    score SMALLINT CHECK (score BETWEEN 0 AND 100),
    horizon_years INT,
    drawdown_tolerance NUMERIC(5,2),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Goals
CREATE TABLE IF NOT EXISTS goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    "name" TEXT NOT NULL,
    target_amount NUMERIC(20,2) NOT NULL,
    target_date DATE NOT NULL,
    "priority" SMALLINT DEFAULT 1,
    funding_rate NUMERIC(5,2),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Insights
CREATE TABLE IF NOT EXISTS insights (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    "type" TEXT,
    payload_json JSONB
);

-- Recommendations (final coach outputs)
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    "action" TEXT CHECK (action IN ('BUY','SELL','HOLD','REBALANCE','DIVERSIFY','HEDGE')),
    confidence NUMERIC(3,2),
    rationale_md TEXT,
    diffs_json JSONB
);

-- News signals
CREATE TABLE IF NOT EXISTS news_signals (
    id BIGSERIAL PRIMARY KEY,
    symbol TEXT NOT NULL,
    ts TIMESTAMPTZ NOT NULL,
    probs_json JSONB,
    sentiment NUMERIC(3,2),
    source TEXT,
    "url" TEXT
);

CREATE INDEX IF NOT EXISTS idx_news_symbol_ts ON news_signals(symbol, ts);

-- LLM audit
CREATE TABLE IF NOT EXISTS audit_llm (
    id BIGSERIAL PRIMARY KEY,
    prompt_hash TEXT NOT NULL,
    model TEXT NOT NULL,
    input_chars INT,
    output_chars INT,
    cost NUMERIC(10,4),
    latency_ms INT,
    created_at TIMESTAMPTZ DEFAULT now()
);