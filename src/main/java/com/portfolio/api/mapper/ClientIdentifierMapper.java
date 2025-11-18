package com.portfolio.api.mapper;

import com.portfolio.api.provider.CoreBankingProvider;
import com.portfolio.api.provider.dto.CustomerData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientIdentifierMapper {

    private final CoreBankingProvider coreBankingProvider;

    public Optional<String> getCpfForClient(Long clienteId) {
        return coreBankingProvider.findCustomerByCpf(clienteId.toString())
                .map(CustomerData::getCpf);
    }

    public boolean clientExists(Long clienteId) {
        return coreBankingProvider.customerExists(clienteId.toString());
    }
}
