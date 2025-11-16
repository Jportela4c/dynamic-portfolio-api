package com.portfolio.api.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RiskProfileCalculator {

    private static final BigDecimal VOLUME_THRESHOLD_MODERATE = new BigDecimal("50000");
    private static final BigDecimal VOLUME_THRESHOLD_AGGRESSIVE = new BigDecimal("150000");
    private static final long FREQUENCY_THRESHOLD_MODERATE = 5;
    private static final long FREQUENCY_THRESHOLD_AGGRESSIVE = 15;

    public int calculateScore(BigDecimal totalVolume, Long transactionCount) {
        int volumeScore = calculateVolumeScore(totalVolume);
        int frequencyScore = calculateFrequencyScore(transactionCount);

        return (int) (volumeScore * 0.5 + frequencyScore * 0.5);
    }

    public String classifyProfile(int score) {
        if (score <= 40) {
            return "Conservador";
        } else if (score <= 70) {
            return "Moderado";
        } else {
            return "Agressivo";
        }
    }

    public String getProfileDescription(String profile) {
        return switch (profile) {
            case "Conservador" -> "Perfil de baixo risco, focado em segurança e liquidez.";
            case "Moderado" -> "Perfil equilibrado entre segurança e rentabilidade.";
            case "Agressivo" -> "Perfil de alto risco, focado em alta rentabilidade.";
            default -> "Perfil não identificado.";
        };
    }

    private int calculateVolumeScore(BigDecimal totalVolume) {
        if (totalVolume == null || totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        if (totalVolume.compareTo(VOLUME_THRESHOLD_AGGRESSIVE) >= 0) {
            return 100;
        } else if (totalVolume.compareTo(VOLUME_THRESHOLD_MODERATE) >= 0) {
            double ratio = totalVolume.subtract(VOLUME_THRESHOLD_MODERATE)
                    .divide(VOLUME_THRESHOLD_AGGRESSIVE.subtract(VOLUME_THRESHOLD_MODERATE), 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            return (int) (40 + (ratio * 60));
        } else {
            double ratio = totalVolume.divide(VOLUME_THRESHOLD_MODERATE, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return (int) (ratio * 40);
        }
    }

    private int calculateFrequencyScore(Long transactionCount) {
        if (transactionCount == null || transactionCount == 0) {
            return 0;
        }

        if (transactionCount >= FREQUENCY_THRESHOLD_AGGRESSIVE) {
            return 100;
        } else if (transactionCount >= FREQUENCY_THRESHOLD_MODERATE) {
            double ratio = (transactionCount - FREQUENCY_THRESHOLD_MODERATE) /
                    (double) (FREQUENCY_THRESHOLD_AGGRESSIVE - FREQUENCY_THRESHOLD_MODERATE);
            return (int) (40 + (ratio * 60));
        } else {
            double ratio = transactionCount / (double) FREQUENCY_THRESHOLD_MODERATE;
            return (int) (ratio * 40);
        }
    }
}
