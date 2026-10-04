package com.advisor.service;

import com.advisor.entity.Holding;
import com.advisor.repository.HoldingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LLMService {

    private final RestTemplate restTemplate;

    @Autowired
    private HoldingRepository holdingRepository;

    public LLMService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Generates a layman-friendly, detailed explanation for a user's portfolio risk score,
     * including portfolio holdings and actionable advice.
     */
    public String generateExplanation(UUID userId, double riskScore) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI financial advisor.\n");
        prompt.append(String.format("User %s has a risk score of %.2f.\n", userId, riskScore));
        prompt.append("Their portfolio holdings are:\n");

        for (Holding h : holdings) {
            prompt.append(String.format("- %s (%s): %.2f%% of portfolio\n",
                    h.getSymbol(), h.getAssetType(), h.getAvgCost()));
        }

        prompt.append("\nProvide a realistic investment recommendation.\n");
        prompt.append("Include actionable advice on holding, selling, or rebalancing, diversification suggestions, and layman-friendly explanations.\n");
        prompt.append("Keep the response concise but informative, using 4-6 sentences or bullet points.\n");

        String url = "http://host.docker.internal:11434/api/generate"; // LLM service endpoint

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "model", "llama2:7b",
                "prompt", prompt.toString(),
                "stream", false
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Could not generate explanation at this time.";
    }
}