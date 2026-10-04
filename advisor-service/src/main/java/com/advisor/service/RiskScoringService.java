package com.advisor.service;

import com.advisor.entity.Holding;
import com.advisor.entity.AssetType;
import com.advisor.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RiskScoringService {

    private final HoldingRepository holdingRepository;

    public RiskScoringService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    /**
     * Calculate risk score for a user portfolio.
     * Returns a normalized score between 0 (safe) and 1 (very risky).
     */
    public double calculateRiskScore(UUID userId, double userRiskTolerance) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        if (holdings.isEmpty()) return 0.0;

        // total portfolio value based on quantity * avgCost
        double totalValue = holdings.stream()
                .mapToDouble(h -> h.getQuantity() * (h.getAvgCost() != null ? h.getAvgCost() : 0.0))
                .sum();

        // 1. Concentration risk: max single asset weight
        Map<String, Double> symbolWeight = holdings.stream()
                .collect(Collectors.toMap(
                        Holding::getSymbol,
                        h -> h.getQuantity() * (h.getAvgCost() != null ? h.getAvgCost() : 0.0) / totalValue,
                        Double::sum // in case of multiple entries for same symbol
                ));

        double concentrationRisk = symbolWeight.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        // 2. Asset type risk: weighted average of type multipliers
        double assetTypeRisk = holdings.stream()
                .mapToDouble(h -> getAssetTypeRisk(h.getAssetType()))
                .average()
                .orElse(0.0);

        // 3. Combine with weights
        double riskScore = 0.5 * concentrationRisk + 0.5 * assetTypeRisk;

        // Adjust based on user risk tolerance
        riskScore = riskScore / userRiskTolerance;
        return Math.min(1.0, riskScore); // normalize to max 1
    }

    private double getAssetTypeRisk(AssetType assetType) {
        if (assetType == null) return 0.5; // default
        switch (assetType) {
            case CRYPTO: return 1.0;
            case STOCK: return 0.7;
            case ETF: return 0.5;
            case BOND: return 0.3;
            case MUTUAL_FUND: return 0.4;
            default: return 0.5;
        }
    }
}