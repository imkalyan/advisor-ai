package com.advisor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "advisor_signals")
public class AdvisorSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private int windowDays;
    private double avgSentiment;
    private double ewmaSentiment;
    private int sampleSize;
    private String lastUpdated;
    private String action;
    private String reason;
    private Double decisionConfidence;
    private String decisionSource;
    private String decisionModel;

    @Column(columnDefinition = "jsonb") // requires Postgres
    private String aggregatedProbabilities;  // store JSON string

    @Column(columnDefinition = "jsonb")
    private String actionProbabilities;

    @Column(columnDefinition = "jsonb")
    private String topArticles;

    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public int getWindowDays() {
        return windowDays;
    }
    public void setWindowDays(int windowDays) {
        this.windowDays = windowDays;
    }
    public double getAvgSentiment() {
        return avgSentiment;
    }
    public void setAvgSentiment(double avgSentiment) {
        this.avgSentiment = avgSentiment;
    }
    public double getEwmaSentiment() {
        return ewmaSentiment;
    }
    public void setEwmaSentiment(double ewmaSentiment) {
        this.ewmaSentiment = ewmaSentiment;
    }
    public int getSampleSize() {
        return sampleSize;
    }
    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getAggregatedProbabilities() {
        return aggregatedProbabilities;
    }
    public void setAggregatedProbabilities(String aggregatedProbabilities) {
        this.aggregatedProbabilities = aggregatedProbabilities;
    }
    public String getReason(){
        return reason;
    }
    public void setReason(String reason){
        this.reason = reason;
    }
    public Double getDecisionConfidence() { return decisionConfidence; }
    public void setDecisionConfidence(Double decisionConfidence) { this.decisionConfidence = decisionConfidence; }
    public String getDecisionSource() { return decisionSource; }
    public void setDecisionSource(String decisionSource) { this.decisionSource = decisionSource; }
    public String getDecisionModel() { return decisionModel; }
    public void setDecisionModel(String decisionModel) { this.decisionModel = decisionModel; }
    public String getActionProbabilities() { return actionProbabilities; }
    public void setActionProbabilities(String actionProbabilities) { this.actionProbabilities = actionProbabilities; }
    public String getTopArticles() { return topArticles; }
    public void setTopArticles(String topArticles) { this.topArticles = topArticles; }
    
}
