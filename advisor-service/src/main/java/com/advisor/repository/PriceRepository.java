package com.advisor.repository;

import com.advisor.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findBySymbolOrderByDateDesc(String symbol);
    Price findFirstBySymbolOrderByDateDesc(String symbol);
    Price findBySymbolAndDate(String symbol, LocalDate date);

    @Query("SELECT DISTINCT h.symbol FROM Holding h")
    List<String> findAllSymbols();
}