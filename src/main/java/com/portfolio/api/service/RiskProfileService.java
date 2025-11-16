package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.util.RiskProfileCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskProfileService {

    private final InvestmentRepository investmentRepository;
    private final RiskProfileCalculator riskProfileCalculator;

    public RiskProfileService(InvestmentRepository investmentRepository,
                              RiskProfileCalculator riskProfileCalculator) {
        this.investmentRepository = investmentRepository;
        this.riskProfileCalculator = riskProfileCalculator;
    }

    public RiskProfileResponse calculateRiskProfile(Long clienteId) {
        Long transactionCount = investmentRepository.countByClienteId(clienteId);
        BigDecimal totalVolume = investmentRepository.sumValorByClienteId(clienteId);

        if (totalVolume == null) {
            totalVolume = BigDecimal.ZERO;
        }

        int score = riskProfileCalculator.calculateScore(totalVolume, transactionCount);
        String profile = riskProfileCalculator.classifyProfile(score);
        String description = riskProfileCalculator.getProfileDescription(profile);

        return RiskProfileResponse.builder()
                .clienteId(clienteId)
                .perfil(profile)
                .pontuacao(score)
                .descricao(description)
                .build();
    }
}
