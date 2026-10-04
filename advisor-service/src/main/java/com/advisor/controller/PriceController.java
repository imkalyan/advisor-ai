package com.advisor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.advisor.service.PriceUpdateService;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceUpdateService priceUpdateService;
    public PriceController(PriceUpdateService priceUpdateService) {
        this.priceUpdateService = priceUpdateService;
    }

    @GetMapping("/manualPriceUpdate")
    public ResponseEntity<String> manualPriceUpdate() {
        // Trigger nightly price update on controller initialization
        return ResponseEntity.ok(priceUpdateService.manualPriceUpdate());
    }
    
}
