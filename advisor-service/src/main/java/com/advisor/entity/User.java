package com.advisor.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "risk_profile")
    private String riskProfile; // CONSERVATIVE, MODERATE, AGGRESSIVE

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
