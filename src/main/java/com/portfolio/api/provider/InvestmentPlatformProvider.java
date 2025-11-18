package com.portfolio.api.provider;

import com.portfolio.api.provider.dto.CustomerPortfolio;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.provider.dto.Position;

import java.util.List;

/**
 * Interface para integração com Sistema de Investimentos.
 * Fornece dados de carteira e histórico de investimentos.
 */
public interface InvestmentPlatformProvider {

    /**
     * Obtém a carteira completa do cliente.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return Dados da carteira com posições e rentabilidade
     */
    CustomerPortfolio getPortfolio(String cpf);

    /**
     * Obtém o histórico de investimentos do cliente (aplicações e resgates).
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return Lista de investimentos históricos
     */
    List<Investment> getInvestmentHistory(String cpf);

    /**
     * Obtém as posições atuais do cliente.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return Lista de posições em aberto
     */
    List<Position> getCurrentPositions(String cpf);
}
