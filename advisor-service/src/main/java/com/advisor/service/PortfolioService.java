package com.advisor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.advisor.entity.Holding;
import com.advisor.repository.HoldingRepository;

@Service
public class PortfolioService {
    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private LLMService llmService;

    public Map<String, Object> analyzePortfolio(UUID userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        double riskScore = riskScoringService.calculateRiskScore(userId, 0.5); // assuming moderate risk tolerance
        String explanation = llmService.generateExplanation(userId, riskScore);

        Map<String, Object> result = new HashMap<>();
        result.put("riskScore", riskScore);
        result.put("explanation", explanation);
        result.put("holdings", holdings);
        return result;
    }
}
