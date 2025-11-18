package com.portfolio.api.provider;

import com.portfolio.api.provider.dto.BehavioralIndicators;
import com.portfolio.api.provider.dto.Transaction;
import com.portfolio.api.provider.dto.TransactionSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface para integração com Sistema de Movimentações.
 * Fornece dados transacionais e indicadores comportamentais.
 */
public interface TransactionSystemProvider {

    /**
     * Obtém resumo de movimentações do cliente em um período.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @param inicio Data de início do período
     * @param fim Data de fim do período
     * @return Resumo de movimentações
     */
    TransactionSummary getTransactionSummary(String cpf, LocalDate inicio, LocalDate fim);

    /**
     * Obtém todas as movimentações do cliente em um período.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @param inicio Data de início do período
     * @param fim Data de fim do período
     * @return Lista de transações
     */
    List<Transaction> getTransactions(String cpf, LocalDate inicio, LocalDate fim);

    /**
     * Obtém indicadores comportamentais do cliente.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return Indicadores de comportamento financeiro
     */
    BehavioralIndicators getBehavioralIndicators(String cpf);
}
