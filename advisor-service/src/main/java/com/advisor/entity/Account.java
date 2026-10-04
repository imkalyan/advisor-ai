package com.advisor.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String broker;

    @Column(name = "type")
    private String type; // BROKERAGE, MF, ETF, CRYPTO, BOND, CASH

    @Column(name = "masked_id")
    private String maskedId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

}
