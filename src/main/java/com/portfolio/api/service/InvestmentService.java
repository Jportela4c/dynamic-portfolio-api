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
    private final ClientValidationService clientValidationService;

    public InvestmentService(InvestmentRepository investmentRepository,
                             ClientValidationService clientValidationService) {
        this.investmentRepository = investmentRepository;
        this.clientValidationService = clientValidationService;
    }

    public List<InvestmentResponse> getClientInvestments(Long clienteId) {
        clientValidationService.validateClientExists(clienteId);

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
