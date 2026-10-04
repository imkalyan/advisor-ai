-- insert demo user
INSERT INTO users(email, risk_profile) 
VALUES ('demo@example.com','moderate') 
ON CONFLICT DO NOTHING;

-- insert account using correct user PK column
INSERT INTO accounts (account_id, user_id, "broker", "type", masked_id, created_at)
VALUES (
    gen_random_uuid(),
    (SELECT id FROM users WHERE email='demo@example.com'),
    'Demo Broker',
    'BROKERAGE',
    'XXXXX',
    now()
) ON CONFLICT DO NOTHING;

-- sample position (TCS on NSE)
INSERT INTO positions(account_id, symbol, market, qty, avg_cost)
SELECT a.account_id, 'TCS', 'NSE', 10, 3500
FROM accounts a 
JOIN users u ON a.user_id = u.id
WHERE u.email='demo@example.com'
ON CONFLICT DO NOTHING;
