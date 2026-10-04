package com.advisor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvisorSignal {

    private final String symbol;
    private final int window_days;
    private final double avg_sentiment;
    private final double ewma_sentiment;
    private final int sample_size;
    private final String last_updated;
    private final String action;
    private final String reason;
    private final Map<String, Double> aggregated_probabilities;
    private final Map<String, Double> action_probabilities;
    private final Double decision_confidence;
    private final String decision_source;
    private final String decision_model;

    private String summary;
    private List<Map<String, Object>> top_articles;

    public List<Map<String, Object>> getTop_articles() { return top_articles; }
    public void setTop_articles(List<Map<String, Object>> top_articles) { this.top_articles = top_articles; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    @JsonCreator
    public AdvisorSignal(
            @JsonProperty("symbol") String symbol,
            @JsonProperty("window_days") int window_days,
            @JsonProperty("avg_sentiment") double avg_sentiment,
            @JsonProperty("ewma_sentiment") double ewma_sentiment,
            @JsonProperty("sample_size") int sample_size,
            @JsonProperty("last_updated") String last_updated,
            @JsonProperty("action") String action,
            @JsonProperty("reason") String reason,
            @JsonProperty("aggregated_probabilities") Map<String, Double> aggregated_probabilities,
            @JsonProperty("action_probabilities") Map<String, Double> action_probabilities,
            @JsonProperty("decision_confidence") Double decision_confidence,
            @JsonProperty("decision_source") String decision_source,
            @JsonProperty("decision_model") String decision_model,
            @JsonProperty("summary") String summary,
            @JsonProperty("top_articles") List<Map<String, Object>> top_articles
    ) {
        this.symbol = symbol;
        this.window_days = window_days;
        this.avg_sentiment = avg_sentiment;
        this.ewma_sentiment = ewma_sentiment;
        this.sample_size = sample_size;
        this.last_updated = last_updated;
        this.action = action;
        this.reason = reason;
        this.aggregated_probabilities = aggregated_probabilities;
        this.action_probabilities = action_probabilities;
        this.decision_confidence = decision_confidence;
        this.decision_source = decision_source;
        this.decision_model = decision_model;
        this.summary = summary;
        this.top_articles = top_articles;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getWindow_days() {
        return window_days;
    }

    public double getAvg_sentiment() {
        return avg_sentiment;
    }

    public double getEwma_sentiment() {
        return ewma_sentiment;
    }

    public int getSample_size() {
        return sample_size;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Double> getAggregated_probabilities() {
        return aggregated_probabilities;
    }
    public Map<String, Double> getAction_probabilities() { return action_probabilities; }
    public Double getDecision_confidence() { return decision_confidence; }
    public String getDecision_source() { return decision_source; }
    public String getDecision_model() { return decision_model; }
    public List<Map<String, Object>> getTopArticles() { return top_articles; }
}
