FROM python:3.11-slim

WORKDIR /app

# Install dependencies
RUN apt-get update && apt-get install -y curl git && rm -rf /var/lib/apt/lists/*

# Install Ollama CLI
RUN curl -fsSL https://ollama.com/install.sh | bash

# Pull the LLaMA2-7B model (local inside container)
#RUN ollama serve && ollama pull llama2:7b

# Install FastAPI and Uvicorn
RUN pip install fastapi uvicorn pydantic requests

COPY ./app /app

EXPOSE 8081
# CMD must run both ollama serve and uvicorn
#CMD ollama serve & uvicorn main:app --host 0.0.0.0 --port 8081