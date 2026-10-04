package com.advisor.service;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.advisor.entity.Price;
import com.advisor.repository.PriceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
@Service
public class PriceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateService.class);
    private final PriceRepository priceRepository;
    private final RestTemplate restTemplate;

    public PriceUpdateService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
        this.restTemplate = new RestTemplate();
        updateNightlyPrices();
    }

    public String manualPriceUpdate() {
        updateNightlyPrices();
        return "Price update completed.";
    }

    // Runs every day at 10 PM
    @Scheduled(cron = "0 0 22 * * *")
    public void updateNightlyPrices() {
        // Fetch symbols from database
        log.info("Starting nightly price update...");
        List<String> symbols = priceRepository.findAllSymbols();
        try {
            List<Map<String, Object>> quotes = fetchPrices(symbols);
            for (Map<String, Object> quote : quotes) {
                String symbol = quote.get("symbol").toString();
                BigDecimal closePrice = new BigDecimal(quote.get("regularMarketPrice").toString());
                log.info("Fetched price for {}: {}", symbol, closePrice);
                if (closePrice != null) {
                    Price price = new Price();
                    price.setSymbol(symbol);
                    price.setDate(LocalDate.now());
                    price.setClosePrice(closePrice.doubleValue());
                    price.setSource("YahooFinance");

                    priceRepository.save(price);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Nightly price update completed.");
    }

    private List<Map<String, Object>> fetchPrices(List<String> symbols) {
        String symbolParam = String.join(",", symbols);
        String url = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=" + symbolParam;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response != null) {
            Map<String, Object> quoteResponse = (Map<String, Object>) response.get("quoteResponse");
            List<Map<String, Object>> results = (List<Map<String, Object>>) quoteResponse.get("result");
            return results;
        }
        return List.of();
    }
}
