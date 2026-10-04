package com.advisor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.advisor.entity.NewsSignal;

public interface NewsSignalRepository extends JpaRepository<NewsSignal, Long> {
    List<NewsSignal> findBySymbolOrderByTsDesc(String symbol);
}
