package com.portfolio.api.provider.impl;

import com.portfolio.api.provider.CoreBankingProvider;
import com.portfolio.api.provider.dto.CustomerData;
import com.portfolio.api.util.CpfValidator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação mock do CoreBankingProvider com dados realistas de clientes brasileiros.
 * Active when: NOT in prod profile (dev, test, default).
 */
@Slf4j
@Service
@Profile("!prod")
public class MockCoreBankingProvider implements CoreBankingProvider {

    private final Map<String, CustomerData> mockCustomers = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing MockCoreBankingProvider with realistic Brazilian customer data");

        mockCustomers.put("12345678900", CustomerData.builder()
                .cpf("12345678900")
                .nome("João Silva Santos")
                .dataNascimento(LocalDate.of(1985, 3, 15))
                .email("joao.silva@example.com")
                .telefone("+5511987654321")
                .endereco(CustomerData.Endereco.builder()
                        .logradouro("Rua das Flores, 123")
                        .bairro("Centro")
                        .cidade("São Paulo")
                        .uf("SP")
                        .cep("01310-100")
                        .build())
                .conta(CustomerData.Conta.builder()
                        .agencia("0001")
                        .numero("123456-7")
                        .tipoConta("CONTA_CORRENTE")
                        .build())
                .perfilAPI("MODERADO")
                .dataAtualizacao(LocalDateTime.now())
                .build());

        mockCustomers.put("98765432100", CustomerData.builder()
                .cpf("98765432100")
                .nome("Maria Santos Oliveira")
                .dataNascimento(LocalDate.of(1990, 7, 22))
                .email("maria.santos@example.com")
                .telefone("+5521976543210")
                .endereco(CustomerData.Endereco.builder()
                        .logradouro("Avenida Paulista, 1000")
                        .bairro("Bela Vista")
                        .cidade("São Paulo")
                        .uf("SP")
                        .cep("01310-200")
                        .build())
                .conta(CustomerData.Conta.builder()
                        .agencia("0002")
                        .numero("234567-8")
                        .tipoConta("CONTA_CORRENTE")
                        .build())
                .perfilAPI("CONSERVADOR")
                .dataAtualizacao(LocalDateTime.now())
                .build());

        mockCustomers.put("11122233344", CustomerData.builder()
                .cpf("11122233344")
                .nome("Pedro Costa Ferreira")
                .dataNascimento(LocalDate.of(1978, 12, 5))
                .email("pedro.costa@example.com")
                .telefone("+5531965432109")
                .endereco(CustomerData.Endereco.builder()
                        .logradouro("Rua dos Investidores, 500")
                        .bairro("Jardins")
                        .cidade("Belo Horizonte")
                        .uf("MG")
                        .cep("30130-100")
                        .build())
                .conta(CustomerData.Conta.builder()
                        .agencia("0003")
                        .numero("345678-9")
                        .tipoConta("CONTA_CORRENTE")
                        .build())
                .perfilAPI("AGRESSIVO")
                .dataAtualizacao(LocalDateTime.now())
                .build());

        mockCustomers.put("44455566677", CustomerData.builder()
                .cpf("44455566677")
                .nome("Ana Paula Rodrigues")
                .dataNascimento(LocalDate.of(1995, 5, 18))
                .email("ana.rodrigues@example.com")
                .telefone("+5541954321098")
                .endereco(CustomerData.Endereco.builder()
                        .logradouro("Rua XV de Novembro, 200")
                        .bairro("Centro")
                        .cidade("Curitiba")
                        .uf("PR")
                        .cep("80020-310")
                        .build())
                .conta(CustomerData.Conta.builder()
                        .agencia("0004")
                        .numero("456789-0")
                        .tipoConta("CONTA_POUPANCA")
                        .build())
                .perfilAPI("MODERADO")
                .dataAtualizacao(LocalDateTime.now())
                .build());

        mockCustomers.put("55566677788", CustomerData.builder()
                .cpf("55566677788")
                .nome("Carlos Eduardo Souza")
                .dataNascimento(LocalDate.of(1982, 9, 30))
                .email("carlos.souza@example.com")
                .telefone("+5585943210987")
                .endereco(CustomerData.Endereco.builder()
                        .logradouro("Avenida Beira Mar, 1500")
                        .bairro("Meireles")
                        .cidade("Fortaleza")
                        .uf("CE")
                        .cep("60165-121")
                        .build())
                .conta(CustomerData.Conta.builder()
                        .agencia("0005")
                        .numero("567890-1")
                        .tipoConta("CONTA_CORRENTE")
                        .build())
                .perfilAPI("CONSERVADOR")
                .dataAtualizacao(LocalDateTime.now())
                .build());

        log.info("MockCoreBankingProvider initialized with {} customers", mockCustomers.size());
    }

    @Override
    public Optional<CustomerData> findCustomerByCpf(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);

        if (!CpfValidator.isValid(normalizedCpf)) {
            log.warn("Invalid CPF format: {}", CpfValidator.mask(cpf));
            return Optional.empty();
        }

        CustomerData customer = mockCustomers.get(normalizedCpf);
        if (customer != null) {
            log.debug("Customer found: {} ({})", customer.getNome(), CpfValidator.mask(normalizedCpf));
        } else {
            log.debug("Customer not found for CPF: {}", CpfValidator.mask(normalizedCpf));
        }

        return Optional.ofNullable(customer);
    }

    @Override
    public boolean customerExists(String cpf) {
        String normalizedCpf = CpfValidator.normalize(cpf);
        return CpfValidator.isValid(normalizedCpf) && mockCustomers.containsKey(normalizedCpf);
    }

    @Override
    public String getCustomerPerfilAPI(String cpf) {
        return findCustomerByCpf(cpf)
                .map(CustomerData::getPerfilAPI)
                .orElse(null);
    }
}
