package com.advisor.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "news_signals")
public class NewsSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Instant ts;

    @Column(name = "probs_json", columnDefinition = "jsonb")
    private String probsJson; // store JSON as String, or use Map<String,Double>

    private BigDecimal sentiment;

    private String source;

    private String url;

    // Getters and Setters
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
    public Instant getTs() {
        return ts;
    }
    public void setTs(Instant ts) {
        this.ts = ts;
    }
    public String getProbsJson() {
        return probsJson;
    }
    public void setProbsJson(String probsJson) {
        this.probsJson = probsJson;
    }
    public BigDecimal getSentiment() {
        return sentiment;
    }
    public void setSentiment(BigDecimal sentiment) {
        this.sentiment = sentiment;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    
}