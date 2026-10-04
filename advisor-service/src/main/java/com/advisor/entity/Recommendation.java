package com.advisor.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String action; // BUY, SELL, HOLD, REBALANCE, DIVERSIFY, HEDGE

    private BigDecimal confidence;

    @Column(name = "rationale_md", columnDefinition = "text")
    private String rationaleMd;

    @Column(name = "diffs_json", columnDefinition = "jsonb")
    private String diffsJson;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}