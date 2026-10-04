package com.advisor.service;

import com.advisor.entity.AssetType;
import com.advisor.entity.Holding;
import com.advisor.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HoldingImportService {

    private final HoldingRepository holdingRepository;

    public HoldingImportService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public void importHoldings(UUID userId, MultipartFile file) {
        List<Holding> holdings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean header = true;

            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // skip header

                String[] cols = line.split(",");

                Holding h = new Holding();
                h.setUserId(userId);
                h.setSymbol(cols[0].trim());
                h.setQuantity(Double.parseDouble(cols[1]));
                h.setAvgCost(Double.parseDouble(cols[2]));
                h.setAssetType(AssetType.valueOf(cols[3].trim().toUpperCase()));

                holdings.add(h);
            }

            holdingRepository.saveAll(holdings);

        } catch (Exception e) {
            throw new RuntimeException("Failed to import holdings: " + e.getMessage(), e);
        }
    }
}