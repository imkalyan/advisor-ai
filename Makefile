KAFKA_BROKER = kafka:9092

consume-news:
	docker exec -it kafka_tools kafka-console-consumer.sh --bootstrap-server $(KAFKA_BROKER) --topic news_sentiment --from-beginning

consume-signals:
	docker exec -it kafka_tools kafka-console-consumer.sh \
	  --bootstrap-server kafka:9092 \
	  --topic news_sentiment \
	  --from-beginning
      
produce-test:
	docker exec -i kafka_tools kafka-console-producer.sh --bootstrap-server $(KAFKA_BROKER) --topic test_topic