package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.util.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EnhancedRiskProfileService {

    private static final Map<String, Double> FACTOR_WEIGHTS = Map.of(
            "volume", 0.25,
            "frequency", 0.20,
            "product_risk", 0.30,
            "liquidity", 0.15,
            "horizon", 0.10
    );

    private final InvestmentVolumeCalculator volumeCalculator;
    private final TransactionFrequencyCalculator frequencyCalculator;
    private final ProductRiskPreferenceCalculator productRiskCalculator;
    private final LiquidityPreferenceCalculator liquidityCalculator;
    private final InvestmentHorizonCalculator horizonCalculator;

    public EnhancedRiskProfileService(InvestmentVolumeCalculator volumeCalculator,
                                      TransactionFrequencyCalculator frequencyCalculator,
                                      ProductRiskPreferenceCalculator productRiskCalculator,
                                      LiquidityPreferenceCalculator liquidityCalculator,
                                      InvestmentHorizonCalculator horizonCalculator) {
        this.volumeCalculator = volumeCalculator;
        this.frequencyCalculator = frequencyCalculator;
        this.productRiskCalculator = productRiskCalculator;
        this.liquidityCalculator = liquidityCalculator;
        this.horizonCalculator = horizonCalculator;
    }

    public RiskProfileResponse calculateRiskProfile(Long clienteId) {
        int volumeScore = volumeCalculator.calculateVolumeScore(clienteId);
        int frequencyScore = frequencyCalculator.calculateFrequencyScore(clienteId);
        int productRiskScore = productRiskCalculator.calculateProductRiskScore(clienteId);
        int liquidityScore = liquidityCalculator.calculateLiquidityScore(clienteId);
        int horizonScore = horizonCalculator.calculateHorizonScore(clienteId);

        int totalScore = (int) Math.round(
                volumeScore * FACTOR_WEIGHTS.get("volume") +
                frequencyScore * FACTOR_WEIGHTS.get("frequency") +
                productRiskScore * FACTOR_WEIGHTS.get("product_risk") +
                liquidityScore * FACTOR_WEIGHTS.get("liquidity") +
                horizonScore * FACTOR_WEIGHTS.get("horizon")
        );

        String profile = classifyProfile(totalScore);
        String description = getProfileDescription(profile);

        Map<String, Integer> factors = new HashMap<>();
        factors.put("volume", volumeScore);
        factors.put("frequencia", frequencyScore);
        factors.put("riscoProdutos", productRiskScore);
        factors.put("liquidez", liquidityScore);
        factors.put("horizonte", horizonScore);

        return RiskProfileResponse.builder()
                .clienteId(clienteId)
                .perfil(profile)
                .pontuacao(totalScore)
                .descricao(description)
                .fatores(factors)
                .build();
    }

    private String classifyProfile(int score) {
        if (score <= 20) {
            return "Ultra Conservador";
        } else if (score <= 40) {
            return "Conservador";
        } else if (score <= 55) {
            return "Moderado Conservador";
        } else if (score <= 70) {
            return "Moderado";
        } else if (score <= 85) {
            return "Moderado Agressivo";
        } else {
            return "Agressivo";
        }
    }

    private String getProfileDescription(String profile) {
        return switch (profile) {
            case "Ultra Conservador" -> "Perfil extremamente conservador, prioriza segurança máxima e liquidez imediata.";
            case "Conservador" -> "Perfil de baixo risco, focado em segurança e liquidez.";
            case "Moderado Conservador" -> "Perfil que prioriza segurança mas aceita riscos muito baixos.";
            case "Moderado" -> "Perfil equilibrado entre segurança e rentabilidade.";
            case "Moderado Agressivo" -> "Perfil que busca rentabilidade mas mantém alguma segurança.";
            case "Agressivo" -> "Perfil de alto risco, focado em alta rentabilidade.";
            default -> "Perfil não identificado.";
        };
    }
}
