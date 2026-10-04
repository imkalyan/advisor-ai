package com.advisor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.advisor.entity.AdvisorSignalEntity;
import com.advisor.repository.AdvisorSignalRepository;

@RestController
@RequestMapping("/api")
public class AdviceController {

    private final AdvisorSignalRepository advisorSignalRepository;

    @Autowired
    public AdviceController(AdvisorSignalRepository advisorSignalRepository) {
        this.advisorSignalRepository = advisorSignalRepository;
    }

    @GetMapping("/advisor/ping")
    public String ping() {
        return "Advisor service is up!";
    }

    @GetMapping("/advisor/signals")
    public List<AdvisorSignalEntity> getAllSignals() {
        return advisorSignalRepository.findAll();
    }

    @GetMapping("/advisor/signals/{symbol}")
    public List<AdvisorSignalEntity> getSignalsBySymbol(@PathVariable String symbol) {
        return advisorSignalRepository.findBySymbol(symbol);
    }
}