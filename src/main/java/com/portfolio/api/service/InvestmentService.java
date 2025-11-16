package com.portfolio.api.service;

import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.model.entity.Investment;
import com.portfolio.api.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentService(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    public List<InvestmentResponse> getClientInvestments(Long clienteId) {
        List<Investment> investments = investmentRepository.findByClienteIdOrderByDataDesc(clienteId);

        return investments.stream()
                .map(inv -> InvestmentResponse.builder()
                        .id(inv.getId())
                        .tipo(inv.getTipo())
                        .valor(inv.getValor())
                        .rentabilidade(inv.getRentabilidade())
                        .data(inv.getData())
                        .build())
                .collect(Collectors.toList());
    }
}
