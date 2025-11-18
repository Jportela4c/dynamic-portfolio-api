package com.portfolio.api.mapper;

import com.portfolio.api.model.entity.Client;
import com.portfolio.api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientIdentifierMapper {

    private final ClientRepository clientRepository;

    public Optional<String> getCpfForClient(Long clienteId) {
        return clientRepository.findById(clienteId)
                .map(Client::getCpf);
    }

    public Optional<Long> getClientIdForCpf(String cpf) {
        return clientRepository.findByCpf(cpf)
                .map(Client::getId);
    }

    public boolean clientExists(Long clienteId) {
        return clientRepository.existsById(clienteId);
    }
}
