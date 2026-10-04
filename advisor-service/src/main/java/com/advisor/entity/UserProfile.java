package com.advisor.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID userId;

    private Integer riskScore;  // 1 (low) - 10 (high)

    private String goal;  // e.g., Retirement, House, Education

    private Integer horizonYears;  // Investment horizon

    // getters and setters
}