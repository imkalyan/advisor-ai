# Use a slim Python base image
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Expose port (if you add a REST API or healthcheck endpoint)
EXPOSE 8081

# Run the aggregator
CMD ["./wait-for-it.sh", "kafka:9092", "--", "python", "advisor_signal.py"]