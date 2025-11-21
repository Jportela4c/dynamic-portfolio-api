package com.portfolio.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.mapper.ClientIdentifierMapper;
import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.model.entity.InvestmentDataCache;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.repository.InvestmentDataCacheRepository;
import com.portfolio.api.scorer.FrequencyScorer;
import com.portfolio.api.scorer.HorizonScorer;
import com.portfolio.api.scorer.LiquidityScorer;
import com.portfolio.api.scorer.ProductRiskScorer;
import com.portfolio.api.scorer.AmountScorer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskProfileService {

    // Factor weights based on improvement plan (04-risk-engine-improvement-plan.md)
    private static final Map<String, Double> FACTOR_WEIGHTS = Map.of(
            "amount", 0.25,          // Investment Volume
            "frequency", 0.20,       // Transaction Frequency
            "product_risk", 0.30,    // Product Risk Preference (THE SPEC: "Preferência por liquidez ou rentabilidade")
            "liquidity", 0.15,       // Liquidity Preference
            "horizon", 0.10          // Investment Horizon
    );

    private final AmountScorer amountCalculator;
    private final FrequencyScorer frequencyCalculator;
    private final ProductRiskScorer productRiskCalculator;
    private final LiquidityScorer liquidityCalculator;
    private final HorizonScorer horizonCalculator;
    private final CustomerValidationService customerValidationService;
    private final InvestmentPlatformProvider investmentPlatformProvider;
    private final ClientIdentifierMapper clientIdentifierMapper;
    private final InvestmentDataCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;

    public RiskProfileResponse calculateRiskProfile(Long clienteId) {
        customerValidationService.validateClientExists(clienteId);

        String cpf = clientIdentifierMapper.getCpfForClient(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("CPF not found for clienteId: " + clienteId));

        List<Investment> investments = fetchInvestmentsWithResilience(cpf);

        int amountScore = amountCalculator.calculateAmountScore(investments);
        int frequencyScore = frequencyCalculator.calculateFrequencyScore(investments);
        int productRiskScore = productRiskCalculator.calculateProductRiskScore(investments);
        int liquidityScore = liquidityCalculator.calculateLiquidityScore(investments);
        int horizonScore = horizonCalculator.calculateHorizonScore(investments);

        int totalScore = (int) Math.round(
                amountScore * FACTOR_WEIGHTS.get("amount") +
                frequencyScore * FACTOR_WEIGHTS.get("frequency") +
                productRiskScore * FACTOR_WEIGHTS.get("product_risk") +
                liquidityScore * FACTOR_WEIGHTS.get("liquidity") +
                horizonScore * FACTOR_WEIGHTS.get("horizon")
        );

        PerfilRisco profile = classifyProfile(totalScore);
        String description = getProfileDescription(profile);

        return RiskProfileResponse.builder()
                .clienteId(clienteId)
                .perfil(profile)
                .pontuacao(totalScore)
                .descricao(description)
                .build();
    }

    @CircuitBreaker(name = "ofbProvider", fallbackMethod = "fetchInvestmentsFallback")
    @Retry(name = "ofbProvider")
    @Cacheable(value = "portfolio-primary", key = "#cpf")
    private List<Investment> fetchInvestmentsWithResilience(String cpf) {
        List<Investment> investments = investmentPlatformProvider.getInvestmentHistory(cpf);

        updatePersistentCache(cpf, investments);

        return investments;
    }

    private List<Investment> fetchInvestmentsFallback(String cpf, Exception ex) {
        log.warn("Provider unavailable, trying persistent cache for CPF: {}", maskCpf(cpf));

        return cacheRepository.findValidCacheByCpf(cpf, LocalDateTime.now())
                .map(this::deserializeInvestments)
                .orElseGet(() -> {
                    log.error("No cached data available for CPF: {}", maskCpf(cpf));
                    return List.of();
                });
    }

    private void updatePersistentCache(String cpf, List<Investment> investments) {
        try {
            String json = objectMapper.writeValueAsString(investments);
            InvestmentDataCache cache = InvestmentDataCache.builder()
                    .cpf(cpf)
                    .investmentData(json)
                    .fetchedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            cacheRepository.save(cache);
        } catch (Exception e) {
            log.error("Failed to update persistent cache for CPF: {}", maskCpf(cpf), e);
        }
    }

    private List<Investment> deserializeInvestments(InvestmentDataCache cache) {
        try {
            return objectMapper.readValue(
                    cache.getInvestmentData(),
                    new TypeReference<List<Investment>>() {}
            );
        } catch (Exception e) {
            log.error("Failed to deserialize cached investments", e);
            return List.of();
        }
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private PerfilRisco classifyProfile(int score) {
        if (score <= 40) {
            return PerfilRisco.CONSERVADOR;
        } else if (score <= 70) {
            return PerfilRisco.MODERADO;
        } else {
            return PerfilRisco.AGRESSIVO;
        }
    }

    private String getProfileDescription(PerfilRisco profile) {
        return switch (profile) {
            case CONSERVADOR -> "Perfil de baixo risco, focado em segurança e liquidez.";
            case MODERADO -> "Perfil equilibrado entre segurança e rentabilidade.";
            case AGRESSIVO -> "Perfil de alto risco, focado em alta rentabilidade.";
        };
    }
}
