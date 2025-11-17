package com.portfolio.api.service;

import com.portfolio.api.exception.CustomerNotFoundException;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.repository.SimulationRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerValidationService {

    private final InvestmentRepository investmentRepository;
    private final SimulationRepository simulationRepository;

    public CustomerValidationService(InvestmentRepository investmentRepository,
                                     SimulationRepository simulationRepository) {
        this.investmentRepository = investmentRepository;
        this.simulationRepository = simulationRepository;
    }

    public void validateClientExists(Long clienteId) {
        if (!clientExists(clienteId)) {
            throw new CustomerNotFoundException(clienteId);
        }
    }

    public boolean clientExists(Long clienteId) {
        return investmentRepository.existsByClienteId(clienteId)
            || simulationRepository.existsByClienteId(clienteId);
    }
}
