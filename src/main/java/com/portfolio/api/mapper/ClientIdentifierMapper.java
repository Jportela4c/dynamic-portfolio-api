package com.portfolio.api.mapper;

import com.portfolio.api.model.entity.Customer;
import com.portfolio.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientIdentifierMapper {

    private final CustomerRepository customerRepository;

    public Optional<String> getCpfForClient(Long clienteId) {
        return customerRepository.findById(clienteId)
                .map(Customer::getCpf);
    }

    public Optional<Long> getClientIdForCpf(String cpf) {
        return customerRepository.findByCpf(cpf)
                .map(Customer::getId);
    }

    public boolean clientExists(Long clienteId) {
        return customerRepository.existsById(clienteId);
    }
}
