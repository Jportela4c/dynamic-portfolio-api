package com.ofb.mock.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ofb.api.model.bankfixedincome.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MockDataService {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Map<String, Object> customers;
    private Map<String, List<Map<String, Object>>> bankFixedIncomesRaw;
    private Map<String, List<Map<String, Object>>> treasuryTitlesRaw;
    private Map<String, List<Map<String, Object>>> fundsRaw;
    private Map<String, List<Map<String, Object>>> creditFixedIncomesRaw;
    private Map<String, List<Map<String, Object>>> variableIncomesRaw;

    @PostConstruct
    public void loadMockData() {
        try {
            log.info("Loading comprehensive OFB mock data...");

            customers = loadJsonFile("mock-data/customers.json");
            bankFixedIncomesRaw = loadJsonFileAsList("mock-data/bank-fixed-incomes.json");
            treasuryTitlesRaw = loadJsonFileAsList("mock-data/treasury-titles.json");
            fundsRaw = loadJsonFileAsList("mock-data/funds.json");
            creditFixedIncomesRaw = loadJsonFileAsList("mock-data/credit-fixed-incomes.json");
            variableIncomesRaw = loadJsonFileAsList("mock-data/variable-incomes.json");

            log.info("Mock data loaded successfully. CPFs: {}", customers.keySet());
            log.info("Bank fixed incomes: {} customers", bankFixedIncomesRaw.size());
            log.info("Treasury titles: {} customers", treasuryTitlesRaw.size());
            log.info("Funds: {} customers", fundsRaw.size());
            log.info("Credit fixed incomes: {} customers", creditFixedIncomesRaw.size());
            log.info("Variable incomes: {} customers", variableIncomesRaw.size());
        } catch (Exception e) {
            log.error("Failed to load mock data", e);
            throw new RuntimeException("Failed to load mock data", e);
        }
    }

    private Map<String, Object> loadJsonFile(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Mock data file not found: " + path);
        }
        return mapper.readValue(is, new TypeReference<>() {});
    }

    private Map<String, List<Map<String, Object>>> loadJsonFileAsList(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new RuntimeException("Mock data file not found: " + path);
        }
        return mapper.readValue(is, new TypeReference<>() {});
    }

    public Object getCustomerByCpf(String cpf) {
        return customers.get(cpf);
    }

    public boolean customerExists(String cpf) {
        return customers.containsKey(cpf);
    }

    /**
     * Returns bank fixed income investments for LIST endpoint (4 fields only).
     * Extracts: brandName, companyCnpj, investmentType, investmentId
     */
    public List<ResponseBankFixedIncomesProductListDataInner> getBankFixedIncomesList(String cpf) {
        List<Map<String, Object>> raw = bankFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList());

        return raw.stream().map(investment -> {
            ResponseBankFixedIncomesProductListDataInner item = new ResponseBankFixedIncomesProductListDataInner();
            item.setBrandName((String) investment.get("brandName"));
            item.setCompanyCnpj((String) investment.get("companyCnpj"));
            item.setInvestmentType(EnumInvestmentType.fromValue((String) investment.get("investmentType")));
            item.setInvestmentId((String) investment.get("investmentId"));
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Returns complete investment details (15+ fields) for a specific investment ID.
     */
    public Map<String, Object> getBankFixedIncomeDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = bankFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList());
        return investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);
    }

    public List<Map<String, Object>> getTreasuryTitlesByCpf(String cpf) {
        return treasuryTitlesRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getFundsByCpf(String cpf) {
        return fundsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getCreditFixedIncomesByCpf(String cpf) {
        return creditFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getVariableIncomesByCpf(String cpf) {
        return variableIncomesRaw.getOrDefault(cpf, Collections.emptyList());
    }
}
