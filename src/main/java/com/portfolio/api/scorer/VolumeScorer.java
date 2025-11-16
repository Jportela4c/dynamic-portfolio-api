package com.portfolio.api.scorer;

import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class VolumeScorer {

    private final InvestmentRepository investmentRepository;

    public VolumeScorer(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateVolumeScore(Long clienteId) {
        BigDecimal clientVolume = investmentRepository.sumValorByClienteId(clienteId);

        if (clientVolume == null || clientVolume.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        List<BigDecimal> allVolumes = investmentRepository.getAllClientVolumes();

        if (allVolumes.isEmpty() || allVolumes.size() == 1) {
            return 50;
        }

        double[] volumesArray = allVolumes.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();

        double clientPercentile = calculatePercentileRank(
                clientVolume.doubleValue(),
                volumesArray
        );

        return (int) Math.round(clientPercentile);
    }

    private double calculatePercentileRank(double value, double[] data) {
        long countBelow = Arrays.stream(data)
                .filter(v -> v < value)
                .count();

        return (countBelow * 100.0) / data.length;
    }
}
