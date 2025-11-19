package com.ofb.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ofb.mock.model.Customer;
import com.ofb.mock.model.Investment;
import com.ofb.mock.model.MockData;
import com.ofb.mock.model.Transaction;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@ApplicationScoped
public class MockDataService {

    @ConfigProperty(name = "mock.data.file")
    String mockDataFile;

    private MockData mockData;

    @PostConstruct
    public void loadMockData() {
        try {
            log.info("Loading mock data from: {}", mockDataFile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            InputStream is = getClass().getClassLoader().getResourceAsStream(mockDataFile);
            if (is == null) {
                throw new RuntimeException("Mock data file not found: " + mockDataFile);
            }

            mockData = mapper.readValue(is, MockData.class);
            log.info("Mock data loaded successfully. CPFs: {}", mockData.getCustomers().keySet());
        } catch (Exception e) {
            log.error("Failed to load mock data", e);
            throw new RuntimeException("Failed to load mock data", e);
        }
    }

    public List<Investment> getInvestmentsByCpf(String cpf) {
        return mockData.getInvestments().getOrDefault(cpf, Collections.emptyList());
    }

    public Customer getCustomerByCpf(String cpf) {
        return mockData.getCustomers().get(cpf);
    }

    public List<Transaction> getTransactionsByCpf(String cpf) {
        return mockData.getTransactions().getOrDefault(cpf, Collections.emptyList());
    }

    public boolean customerExists(String cpf) {
        return mockData.getCustomers().containsKey(cpf);
    }

    public List<Investment> getAllInvestments() {
        return mockData.getInvestments().values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public Investment getInvestmentById(String investmentId) {
        return getAllInvestments().stream()
                .filter(inv -> inv.getInvestmentId().equals(investmentId))
                .findFirst()
                .orElse(null);
    }
}
