package com.advisor.controller;

import com.advisor.entity.Holding;
import com.advisor.repository.HoldingRepository;
import com.advisor.service.HoldingImportService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingImportService holdingImportService;
    private final HoldingRepository holdingService;

    public HoldingController(HoldingImportService holdingImportService, HoldingRepository holdingService) {
        this.holdingImportService = holdingImportService;
        this.holdingService = holdingService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importHoldings(
            @RequestParam UUID userId,
            @RequestParam("file") MultipartFile file) {
        holdingImportService.importHoldings(userId, file);
        return ResponseEntity.ok("Holdings imported successfully.");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Holding>> getUserHoldings(@PathVariable UUID userId) {
        return ResponseEntity.ok(holdingService.findByUserId(userId));
    }
}