ALTER TABLE advisor_signals
    ADD COLUMN IF NOT EXISTS action_probabilities JSONB,
    ADD COLUMN IF NOT EXISTS decision_confidence DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS decision_source VARCHAR(64),
    ADD COLUMN IF NOT EXISTS decision_model VARCHAR(128),
    ADD COLUMN IF NOT EXISTS top_articles JSONB;
