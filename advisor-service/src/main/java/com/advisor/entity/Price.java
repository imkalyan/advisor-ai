package com.advisor.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prices")
@IdClass(PriceId.class)
public class Price {


    @Id
    private String symbol;

    @Id
    private LocalDate date;

    private Double closePrice;

    private String source; // e.g., "YahooFinance"

    // getters and setters

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Double getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

}
