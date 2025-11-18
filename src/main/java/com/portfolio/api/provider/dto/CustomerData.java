package com.portfolio.api.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dados cadastrais do cliente vindos do Core Banking System.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerData {

    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private Endereco endereco;
    private Conta conta;
    private String perfilAPI;
    private LocalDateTime dataAtualizacao;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Endereco {
        private String logradouro;
        private String bairro;
        private String cidade;
        private String uf;
        private String cep;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conta {
        private String agencia;
        private String numero;
        private String tipoConta;
    }
}
