insert into users(email, risk_profile) values ('alice@example.com','aggressive') on conflict do nothing;

insert into users(email, risk_profile) values ('bob@example.com','balanced') on conflict do nothing;

insert into positions(account_id, symbol, market, qty, avg_cost)
select a.account_id, 'HDFCBANK', 'NSE', 25, 1500
from accounts a join users u on a.user_id=u.id
where u.email='alice@example.com'
on conflict do nothing;

insert into positions(account_id, symbol, market, qty, avg_cost)
select a.account_id, 'SBIN', 'NSE', 30, 350
from accounts a join users u on a.user_id=u.id
where u.email='alice@example.com'
on conflict do nothing;

insert into positions(account_id, symbol, market, qty, avg_cost)
select a.account_id, 'ITC', 'NSE', 40, 210
from accounts a join users u on a.user_id=u.id
where u.email='bob@example.com'
on conflict do nothing;

insert into positions(account_id, symbol, market, qty, avg_cost)
select a.account_id, 'SBIN', 'NSE', 20, 355
from accounts a join users u on a.user_id=u.id
where u.email='bob@example.com'
on conflict do nothing;

INSERT INTO prices(symbol, "date", "close", fx, source)
VALUES
('AAPL', '2025-08-27', 180.25, 1.0, 'Yahoo'),
('MSFT', '2025-08-27', 310.50, 1.0, 'Yahoo')
ON CONFLICT (symbol, "date") DO NOTHING;