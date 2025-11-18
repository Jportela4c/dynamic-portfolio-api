package com.portfolio.api.service;

import com.portfolio.api.exception.CustomerNotFoundException;
import com.portfolio.api.provider.CoreBankingProvider;
import com.portfolio.api.repository.InvestmentRepository;
import com.portfolio.api.repository.SimulationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço de validação de clientes.
 * Integra com Core Banking System para verificar existência de clientes.
 */
@Slf4j
@Service
public class CustomerValidationService {

    private final InvestmentRepository investmentRepository;
    private final SimulationRepository simulationRepository;
    private final CoreBankingProvider coreBankingProvider;

    public CustomerValidationService(InvestmentRepository investmentRepository,
                                     SimulationRepository simulationRepository,
                                     CoreBankingProvider coreBankingProvider) {
        this.investmentRepository = investmentRepository;
        this.simulationRepository = simulationRepository;
        this.coreBankingProvider = coreBankingProvider;
    }

    public void validateClientExists(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new CustomerNotFoundException(clienteId);
        }
        if (!clientExists(clienteId)) {
            throw new CustomerNotFoundException(clienteId);
        }
    }

    public boolean clientExists(Long clienteId) {
        return investmentRepository.existsByClienteId(clienteId)
            || simulationRepository.existsByClienteId(clienteId);
    }

    /**
     * Valida se um cliente existe no Core Banking System por CPF.
     *
     * @param cpf CPF do cliente
     * @return true se o cliente existe, false caso contrário
     */
    public boolean clientExistsByCpf(String cpf) {
        return coreBankingProvider.customerExists(cpf);
    }

    /**
     * Obtém o perfil API pré-definido do cliente no Core Banking System.
     *
     * @param cpf CPF do cliente
     * @return Perfil API (CONSERVADOR, MODERADO, AGRESSIVO) ou null se não encontrado
     */
    public String getCustomerPerfilAPI(String cpf) {
        return coreBankingProvider.getCustomerPerfilAPI(cpf);
    }
}
