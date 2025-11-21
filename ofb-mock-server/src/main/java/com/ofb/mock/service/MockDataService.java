package com.ofb.mock.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ofb.api.model.bankfixedincome.*;
import com.ofb.api.model.creditfixedincome.CreditFixedIdentification;
import com.ofb.api.model.fund.ResponseFundsProductIdentificationData;
import com.ofb.api.model.treasuretitle.TreasureTitlesIdentifyProduct;
import com.ofb.api.model.variableincome.ResponseVariableIncomesProductIdentificationData;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mock data service that loads and manages OFB investment data.
 *
 * IMPORTANT: After regenerating mock data with flat structure matching OFB models,
 * this service can deserialize JSON directly into generated OFB types using Jackson.
 *
 * The generated OFB models have @JsonProperty annotations that match the JSON field names.
 */
@Slf4j
@Startup
@ApplicationScoped
public class MockDataService {

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Map<String, Object> customers;
    // TODO: After mvn compile generates models, change to:
    // private Map<String, List<IdentifyProduct>> bankFixedIncomes;
    private Map<String, List<Map<String, Object>>> bankFixedIncomesRaw;
    private Map<String, List<Map<String, Object>>> treasuryTitlesRaw;
    private Map<String, List<Map<String, Object>>> fundsRaw;
    private Map<String, List<Map<String, Object>>> creditFixedIncomesRaw;
    private Map<String, List<Map<String, Object>>> variableIncomesRaw;

    // Transaction data for each investment type
    private Map<String, List<Map<String, Object>>> bankFixedTransactionsRaw;
    private Map<String, List<Map<String, Object>>> treasuryTransactionsRaw;
    private Map<String, List<Map<String, Object>>> fundsTransactionsRaw;
    private Map<String, List<Map<String, Object>>> creditFixedTransactionsRaw;
    private Map<String, List<Map<String, Object>>> variableTransactionsRaw;

    @PostConstruct
    public void loadMockData() {
        try {
            log.info("Loading comprehensive OFB mock data...");

            customers = loadJsonFile("mock-data/customers.json");
            bankFixedIncomesRaw = loadJsonFileAsList("mock-data/bank_fixed_incomes.json");
            treasuryTitlesRaw = loadJsonFileAsList("mock-data/treasury_titles.json");
            fundsRaw = loadJsonFileAsList("mock-data/funds.json");
            creditFixedIncomesRaw = loadJsonFileAsList("mock-data/credit_fixed_incomes.json");
            variableIncomesRaw = loadJsonFileAsList("mock-data/variable_incomes.json");

            // Load transaction data
            bankFixedTransactionsRaw = loadJsonFileAsList("mock-data/bank_fixed_incomes_transactions.json");
            treasuryTransactionsRaw = loadJsonFileAsList("mock-data/treasury_titles_transactions.json");
            fundsTransactionsRaw = loadJsonFileAsList("mock-data/funds_transactions.json");
            creditFixedTransactionsRaw = loadJsonFileAsList("mock-data/credit_fixed_incomes_transactions.json");
            variableTransactionsRaw = loadJsonFileAsList("mock-data/variable_incomes_transactions.json");

            log.info("Mock data loaded successfully. CPFs: {}", customers.keySet());
            log.info("Bank fixed incomes: {} customers", bankFixedIncomesRaw.size());
            log.info("Treasury titles: {} customers", treasuryTitlesRaw.size());
            log.info("Funds: {} customers", fundsRaw.size());
            log.info("Credit fixed incomes: {} customers", creditFixedIncomesRaw.size());
            log.info("Variable incomes: {} customers", variableIncomesRaw.size());

            int totalTransactions = bankFixedTransactionsRaw.values().stream().mapToInt(List::size).sum()
                + treasuryTransactionsRaw.values().stream().mapToInt(List::size).sum()
                + fundsTransactionsRaw.values().stream().mapToInt(List::size).sum()
                + creditFixedTransactionsRaw.values().stream().mapToInt(List::size).sum()
                + variableTransactionsRaw.values().stream().mapToInt(List::size).sum();
            log.info("Total transactions loaded: {}", totalTransactions);
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
     * Uses ObjectMapper.convertValue() to convert Map to typed OFB model.
     * Jackson ignores extra fields (brandName, companyCnpj, investmentId) that don't exist in IdentifyProduct.
     */
    public IdentifyProduct getBankFixedIncomeDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = bankFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList());
        Map<String, Object> investmentMap = investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);

        if (investmentMap == null) {
            return null;
        }

        return mapper.convertValue(investmentMap, IdentifyProduct.class);
    }

    /**
     * Returns treasury title details using typed OFB model.
     */
    public TreasureTitlesIdentifyProduct getTreasuryTitleDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = treasuryTitlesRaw.getOrDefault(cpf, Collections.emptyList());
        Map<String, Object> investmentMap = investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);

        if (investmentMap == null) {
            return null;
        }

        return mapper.convertValue(investmentMap, TreasureTitlesIdentifyProduct.class);
    }

    /**
     * Returns credit fixed income details using typed OFB model.
     */
    public CreditFixedIdentification getCreditFixedIncomeDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = creditFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList());
        Map<String, Object> investmentMap = investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);

        if (investmentMap == null) {
            return null;
        }

        return mapper.convertValue(investmentMap, CreditFixedIdentification.class);
    }

    /**
     * Returns fund details using typed OFB model.
     */
    public ResponseFundsProductIdentificationData getFundDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = fundsRaw.getOrDefault(cpf, Collections.emptyList());
        Map<String, Object> investmentMap = investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);

        if (investmentMap == null) {
            return null;
        }

        return mapper.convertValue(investmentMap, ResponseFundsProductIdentificationData.class);
    }

    /**
     * Returns variable income details using typed OFB model.
     */
    public ResponseVariableIncomesProductIdentificationData getVariableIncomeDetails(String cpf, String investmentId) {
        List<Map<String, Object>> investments = variableIncomesRaw.getOrDefault(cpf, Collections.emptyList());
        Map<String, Object> investmentMap = investments.stream()
            .filter(inv -> investmentId.equals(inv.get("investmentId")))
            .findFirst()
            .orElse(null);

        if (investmentMap == null) {
            return null;
        }

        return mapper.convertValue(investmentMap, ResponseVariableIncomesProductIdentificationData.class);
    }

    /**
     * Returns all raw investment data for a CPF (used by main API for risk profiling).
     * Combines all 6 investment types with complete details.
     */
    public List<Map<String, Object>> getAllInvestmentsByCpf(String cpf) {
        List<Map<String, Object>> allInvestments = new java.util.ArrayList<>();
        allInvestments.addAll(bankFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList()));
        allInvestments.addAll(treasuryTitlesRaw.getOrDefault(cpf, Collections.emptyList()));
        allInvestments.addAll(fundsRaw.getOrDefault(cpf, Collections.emptyList()));
        allInvestments.addAll(creditFixedIncomesRaw.getOrDefault(cpf, Collections.emptyList()));
        allInvestments.addAll(variableIncomesRaw.getOrDefault(cpf, Collections.emptyList()));
        return allInvestments;
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

    /**
     * Returns all transactions for a specific investment across all types.
     * Used for risk profiling based on transaction frequency.
     */
    public List<Map<String, Object>> getTransactionsByInvestmentId(String cpf, String investmentId) {
        List<Map<String, Object>> allTransactions = new java.util.ArrayList<>();

        // Search in all transaction types
        allTransactions.addAll(filterTransactionsByInvestmentId(
            bankFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList()), investmentId));
        allTransactions.addAll(filterTransactionsByInvestmentId(
            treasuryTransactionsRaw.getOrDefault(cpf, Collections.emptyList()), investmentId));
        allTransactions.addAll(filterTransactionsByInvestmentId(
            fundsTransactionsRaw.getOrDefault(cpf, Collections.emptyList()), investmentId));
        allTransactions.addAll(filterTransactionsByInvestmentId(
            creditFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList()), investmentId));
        allTransactions.addAll(filterTransactionsByInvestmentId(
            variableTransactionsRaw.getOrDefault(cpf, Collections.emptyList()), investmentId));

        return allTransactions;
    }

    /**
     * Returns all transactions for a customer (used for risk profiling).
     * Transaction frequency determines risk profile per THE SPEC.
     */
    public List<Map<String, Object>> getAllTransactionsByCpf(String cpf) {
        List<Map<String, Object>> allTransactions = new java.util.ArrayList<>();
        allTransactions.addAll(bankFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList()));
        allTransactions.addAll(treasuryTransactionsRaw.getOrDefault(cpf, Collections.emptyList()));
        allTransactions.addAll(fundsTransactionsRaw.getOrDefault(cpf, Collections.emptyList()));
        allTransactions.addAll(creditFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList()));
        allTransactions.addAll(variableTransactionsRaw.getOrDefault(cpf, Collections.emptyList()));
        return allTransactions;
    }

    public List<Map<String, Object>> getBankFixedTransactions(String cpf) {
        return bankFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getTreasuryTransactions(String cpf) {
        return treasuryTransactionsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getFundsTransactions(String cpf) {
        return fundsTransactionsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getCreditFixedTransactions(String cpf) {
        return creditFixedTransactionsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    public List<Map<String, Object>> getVariableTransactions(String cpf) {
        return variableTransactionsRaw.getOrDefault(cpf, Collections.emptyList());
    }

    private List<Map<String, Object>> filterTransactionsByInvestmentId(List<Map<String, Object>> transactions, String investmentId) {
        return transactions.stream()
            .filter(tx -> {
                String txId = (String) tx.get("transactionId");
                return txId != null && txId.startsWith("TX-" + investmentId);
            })
            .collect(Collectors.toList());
    }

    /**
     * Generate mock balance data for any investment type per OFB spec.
     * Returns balance with netAmount (current value) and grossAmount (invested + growth before taxes).
     */
    public Map<String, Object> generateBalanceData(String cpf, String investmentId) {
        // Use investmentId hash to generate consistent mock data
        int seed = investmentId.hashCode();
        java.util.Random random = new java.util.Random(seed);

        // Generate invested amount between 5k-100k
        double investedAmount = 5000 + (random.nextDouble() * 95000);

        // Generate growth between -5% and +20%
        double growthPercent = -0.05 + (random.nextDouble() * 0.25);
        double grossAmount = investedAmount * (1 + growthPercent);

        // Tax deduction 15-20% of profit
        double profit = grossAmount - investedAmount;
        double taxRate = 0.15 + (random.nextDouble() * 0.05);
        double taxAmount = profit > 0 ? profit * taxRate : 0;
        double netAmount = grossAmount - taxAmount;

        // Build balance response per OFB spec
        Map<String, Object> balanceData = new java.util.HashMap<>();
        balanceData.put("referenceDate", java.time.LocalDate.now().toString());

        Map<String, Object> grossAmountObj = new java.util.HashMap<>();
        grossAmountObj.put("amount", grossAmount);
        grossAmountObj.put("currency", "BRL");
        balanceData.put("grossAmount", grossAmountObj);

        Map<String, Object> netAmountObj = new java.util.HashMap<>();
        netAmountObj.put("amount", netAmount);
        netAmountObj.put("currency", "BRL");
        balanceData.put("netAmount", netAmountObj);

        Map<String, Object> incomeTaxObj = new java.util.HashMap<>();
        incomeTaxObj.put("amount", taxAmount);
        incomeTaxObj.put("currency", "BRL");
        balanceData.put("incomeTaxProvision", incomeTaxObj);

        // For funds: add quota data per OFB Funds spec
        if (investmentId.startsWith("FUND-")) {
            double quotaQuantity = 100 + (random.nextDouble() * 900); // 100-1000 quotas
            double quotaPrice = netAmount / quotaQuantity;

            balanceData.put("quotaQuantity", quotaQuantity);

            Map<String, Object> quotaPriceObj = new java.util.HashMap<>();
            quotaPriceObj.put("amount", quotaPrice);
            quotaPriceObj.put("currency", "BRL");
            balanceData.put("quotaGrossPriceValue", quotaPriceObj);
        }

        // For bank/credit fixed incomes: add quantity per OFB spec
        if (investmentId.startsWith("BANK-") || investmentId.startsWith("CREDIT-")) {
            double quantity = 1 + (random.nextInt(10)); // 1-10 units
            balanceData.put("quantity", quantity);
        }

        return balanceData;
    }
}
