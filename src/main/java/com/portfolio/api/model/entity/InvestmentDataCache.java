package com.portfolio.api.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "investment_data_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentDataCache {

    @Id
    @Column(length = 11)
    private String cpf;

    @Column(name = "investment_data", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String investmentData;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = fetchedAt.plusHours(24);
        }
    }
}
