package com.portfolio.api.scorer;

import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class AmountScorer {

    private final InvestmentRepository investmentRepository;

    public AmountScorer(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateAmountScore(Long clienteId) {
        BigDecimal customerAmount = investmentRepository.sumValorByClienteId(clienteId);

        if (customerAmount == null || customerAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        List<BigDecimal> allVolumes = investmentRepository.getAllCustomerAmounts();

        if (allVolumes.isEmpty() || allVolumes.size() == 1) {
            return 50;
        }

        double[] amountsArray = allVolumes.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();

        double clientPercentile = calculatePercentileRank(
                customerAmount.doubleValue(),
                amountsArray
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
