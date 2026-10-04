package com.advisor.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "cost_basis")
    private Double avgCost;  // keep field name as avgCost, maps to cost_basis in DB

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type")
    private AssetType assetType;  // STOCK, ETF, CRYPTO, BOND, MUTUAL_FUND

    @Column(name = "as_of")
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Double getQuantity() {
        return quantity;
    }
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    public Double getAvgCost() {
        return avgCost;
    }
    public void setAvgCost(Double avgCost) {
        this.avgCost = avgCost;
    }
    public AssetType getAssetType() {
        return assetType;
    }
    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}