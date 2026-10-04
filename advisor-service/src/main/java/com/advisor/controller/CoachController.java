package com.advisor.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.advisor.dto.CoachAnalysisResponse;
import com.advisor.service.LLMService;
import com.advisor.service.RiskScoringService;

@RestController
@RequestMapping("/api/coach")
public class CoachController {

    private final RiskScoringService riskScoringService;
    private final LLMService llmService; // wraps your HTTP call to LLM

    public CoachController(RiskScoringService riskScoringService, LLMService llmService) {
        this.riskScoringService = riskScoringService;
        this.llmService = llmService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<CoachAnalysisResponse> analyzePortfolio(
            @RequestParam UUID userId,
            @RequestParam(required = false, defaultValue = "0.5") double riskTolerance) {

        double riskScore = riskScoringService.calculateRiskScore(userId, riskTolerance);

        // Call LLM to generate explanation
        String explanation = llmService.generateExplanation(userId, riskScore);

        CoachAnalysisResponse response = new CoachAnalysisResponse(riskScore, explanation);
        return ResponseEntity.ok(response);
    }

    
}