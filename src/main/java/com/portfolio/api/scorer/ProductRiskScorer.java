package com.portfolio.api.scorer;

import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductRiskScorer {

    private static final Map<TipoProduto, Integer> PRODUCT_RISK_LEVELS = Map.of(
            TipoProduto.TESOURO_DIRETO, 2,
            TipoProduto.LCI, 3,
            TipoProduto.LCA, 3,
            TipoProduto.CDB, 4,
            TipoProduto.FUNDO_RENDA_FIXA, 5,
            TipoProduto.FUNDO_MULTIMERCADO, 7,
            TipoProduto.FUNDO_ACOES, 9,
            TipoProduto.FII, 8
    );

    private final InvestmentRepository investmentRepository;

    public ProductRiskScorer(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public int calculateProductRiskScore(Long clienteId) {
        List<Investment> investments = investmentRepository
                .findByClienteIdOrderByDataDesc(clienteId);

        if (investments.isEmpty()) {
            return 50;
        }

        double totalValue = investments.stream()
                .mapToDouble(i -> i.getValor().doubleValue())
                .sum();

        double weightedRiskSum = investments.stream()
                .mapToDouble(i -> {
                    int riskLevel = PRODUCT_RISK_LEVELS.getOrDefault(
                            i.getTipo(),
                            5
                    );
                    double weight = i.getValor().doubleValue() / totalValue;
                    return riskLevel * weight;
                })
                .sum();

        return (int) Math.round((weightedRiskSum / 10.0) * 100);
    }
}
