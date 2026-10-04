package com.advisor.repository;

import com.advisor.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUserId(UUID userId);
}