package com.portfolio.api.provider;

import com.portfolio.api.provider.dto.CustomerData;

import java.util.Optional;

/**
 * Interface para integração com Core Banking System (CBS).
 * Fornece dados cadastrais e de conta de clientes.
 */
public interface CoreBankingProvider {

    /**
     * Busca dados completos de um cliente por CPF.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return CustomerData se encontrado, Optional.empty() caso contrário
     */
    Optional<CustomerData> findCustomerByCpf(String cpf);

    /**
     * Verifica se um cliente existe no sistema bancário.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return true se o cliente existe, false caso contrário
     */
    boolean customerExists(String cpf);

    /**
     * Obtém o perfil API pré-definido do cliente no sistema bancário.
     *
     * @param cpf CPF do cliente (11 dígitos)
     * @return Perfil API (CONSERVADOR, MODERADO, AGRESSIVO)
     */
    String getCustomerPerfilAPI(String cpf);
}
