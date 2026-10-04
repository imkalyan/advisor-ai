FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY news_producer.py .
COPY wait-for-it.sh .
COPY ./data/nasdaq-listed.csv .

CMD ["./wait-for-it.sh", "kafka:9092", "--", "python", "news_producer.py"]