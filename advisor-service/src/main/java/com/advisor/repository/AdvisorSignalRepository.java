package com.advisor.repository;

import com.advisor.entity.AdvisorSignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdvisorSignalRepository extends JpaRepository<AdvisorSignalEntity, Long> {
    List<AdvisorSignalEntity> findBySymbol(String symbol);

    @Modifying
        @Query(value = "INSERT INTO advisor_signals (symbol, window_days, avg_sentiment, ewma_sentiment, sample_size, last_updated, action, aggregated_probabilities, reason, action_probabilities, decision_confidence, decision_source, decision_model, top_articles) " +
               "VALUES (:symbol, :windowDays, :avgSentiment, :ewmaSentiment, :sampleSize, :lastUpdated, :action, CAST(:aggregatedProbabilities AS jsonb), :reason, CAST(:actionProbabilities AS jsonb), :decisionConfidence, :decisionSource, :decisionModel, CAST(:topArticles AS jsonb))",
       nativeQuery = true)
    void insertSignal(String symbol, int windowDays, double avgSentiment, double ewmaSentiment,
                      int sampleSize, String lastUpdated, String action, String aggregatedProbabilities, String reason,
                      String actionProbabilities, Double decisionConfidence, String decisionSource,
                      String decisionModel, String topArticles);
}
