package com.advisor.service;

import com.advisor.entity.AdvisorSignalEntity;
import com.advisor.model.AdvisorSignal;
import com.advisor.repository.AdvisorSignalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdvisorSignalConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AdvisorSignalRepository repository;

    public AdvisorSignalConsumer(AdvisorSignalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @KafkaListener(topics = "advisor_signals", groupId = "advisor-service-group")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            AdvisorSignal signal = objectMapper.readValue(message, AdvisorSignal.class);

            AdvisorSignalEntity entity = new AdvisorSignalEntity();
            entity.setSymbol(signal.getSymbol());
            entity.setWindowDays(signal.getWindow_days());
            entity.setAvgSentiment(signal.getAvg_sentiment());
            entity.setEwmaSentiment(signal.getEwma_sentiment());
            entity.setSampleSize(signal.getSample_size());
            entity.setLastUpdated(signal.getLast_updated());
            entity.setAction(signal.getAction());
            entity.setAggregatedProbabilities(objectMapper.writeValueAsString(signal.getAggregated_probabilities()));
            entity.setReason(signal.getReason());
            entity.setActionProbabilities(objectMapper.writeValueAsString(signal.getAction_probabilities()));
            entity.setDecisionConfidence(signal.getDecision_confidence());
            entity.setDecisionSource(signal.getDecision_source());
            entity.setDecisionModel(signal.getDecision_model());
            entity.setTopArticles(objectMapper.writeValueAsString(signal.getTopArticles()));
            
            repository.insertSignal(
                    entity.getSymbol(),
                    entity.getWindowDays(),
                    entity.getAvgSentiment(),
                    entity.getEwmaSentiment(),
                    entity.getSampleSize(),
                    entity.getLastUpdated(),
                    entity.getAction(),
                    objectMapper.writeValueAsString(signal.getAggregated_probabilities()),
                    entity.getReason(), entity.getActionProbabilities(), entity.getDecisionConfidence(),
                    entity.getDecisionSource(), entity.getDecisionModel(), entity.getTopArticles()
            );

            System.out.println("💾 Saved Advisor Signal → " 
                    + signal.getSymbol() 
                    + " (Action: " + signal.getAction() + ", Reason: " + signal.getReason() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
