package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.model.enums.PerfilRisco;
import com.portfolio.api.scorer.FrequencyScorer;
import com.portfolio.api.scorer.HorizonScorer;
import com.portfolio.api.scorer.LiquidityScorer;
import com.portfolio.api.scorer.ProductRiskScorer;
import com.portfolio.api.scorer.AmountScorer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RiskProfileService {

    private static final Map<String, Double> FACTOR_WEIGHTS = Map.of(
            "amount", 0.25,
            "frequency", 0.20,
            "product_risk", 0.30,
            "liquidity", 0.15,
            "horizon", 0.10
    );

    private final AmountScorer amountCalculator;
    private final FrequencyScorer frequencyCalculator;
    private final ProductRiskScorer productRiskCalculator;
    private final LiquidityScorer liquidityCalculator;
    private final HorizonScorer horizonCalculator;
    private final CustomerValidationService customerValidationService;

    public RiskProfileService(AmountScorer amountCalculator,
                                      FrequencyScorer frequencyCalculator,
                                      ProductRiskScorer productRiskCalculator,
                                      LiquidityScorer liquidityCalculator,
                                      HorizonScorer horizonCalculator,
                                      CustomerValidationService customerValidationService) {
        this.amountCalculator = amountCalculator;
        this.frequencyCalculator = frequencyCalculator;
        this.productRiskCalculator = productRiskCalculator;
        this.liquidityCalculator = liquidityCalculator;
        this.horizonCalculator = horizonCalculator;
        this.customerValidationService = customerValidationService;
    }

    public RiskProfileResponse calculateRiskProfile(Long clienteId) {
        customerValidationService.validateClientExists(clienteId);
        int amountScore = amountCalculator.calculateAmountScore(clienteId);
        int frequencyScore = frequencyCalculator.calculateFrequencyScore(clienteId);
        int productRiskScore = productRiskCalculator.calculateProductRiskScore(clienteId);
        int liquidityScore = liquidityCalculator.calculateLiquidityScore(clienteId);
        int horizonScore = horizonCalculator.calculateHorizonScore(clienteId);

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
