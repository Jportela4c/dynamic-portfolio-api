package com.portfolio.api.service.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.mapper.OFBInvestmentMapper;
import com.portfolio.api.provider.dto.OFBInvestmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OFBInvestmentDataService {

    private final OFBBankFixedIncomesClient bankFixedIncomesClient;
    private final OFBCreditFixedIncomesClient creditFixedIncomesClient;
    private final OFBFundsClient fundsClient;
    private final OFBTreasuryTitlesClient treasuryTitlesClient;
    private final OFBVariableIncomesClient variableIncomesClient;
    private final JWSVerificationService jwsVerificationService;
    private final ObjectMapper objectMapper;
    private final OFBInvestmentMapper investmentMapper;

    public List<InvestmentData> fetchInvestments(String accessToken) throws Exception {
        log.info("Fetching investment data from all OFB providers");

        List<InvestmentData> allInvestments = new java.util.ArrayList<>();

        // Fetch from all 5 investment API groups
        allInvestments.addAll(fetchBankFixedIncomes(accessToken));
        allInvestments.addAll(fetchCreditFixedIncomes(accessToken));
        allInvestments.addAll(fetchFunds(accessToken));
        allInvestments.addAll(fetchTreasuryTitles(accessToken));
        allInvestments.addAll(fetchVariableIncomes(accessToken));

        log.info("Successfully fetched {} total investments from OFB provider", allInvestments.size());
        return allInvestments;
    }

    private List<InvestmentData> fetchBankFixedIncomes(String accessToken) {
        try {
            log.info(">>> FETCHING BANK FIXED INCOMES");
            List<InvestmentData> result = fetchInvestmentsByType(
                    accessToken,
                    auth -> bankFixedIncomesClient.getInvestments(auth),
                    (auth, id) -> bankFixedIncomesClient.getInvestmentDetail(auth, id),
                    (auth, id) -> bankFixedIncomesClient.getInvestmentTransactions(auth, id),
                    "Bank Fixed Incomes"
            );
            log.info(">>> BANK FIXED INCOMES RESULT: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error(">>> FAILED TO FETCH BANK FIXED INCOMES", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchCreditFixedIncomes(String accessToken) {
        try {
            log.info(">>> FETCHING CREDIT FIXED INCOMES");
            List<InvestmentData> result = fetchInvestmentsByType(
                    accessToken,
                    auth -> creditFixedIncomesClient.getInvestments(auth),
                    (auth, id) -> creditFixedIncomesClient.getInvestmentDetail(auth, id),
                    (auth, id) -> creditFixedIncomesClient.getInvestmentTransactions(auth, id),
                    "Credit Fixed Incomes"
            );
            log.info(">>> CREDIT FIXED INCOMES RESULT: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error(">>> FAILED TO FETCH CREDIT FIXED INCOMES", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchFunds(String accessToken) {
        try {
            log.info(">>> FETCHING FUNDS");
            List<InvestmentData> result = fetchInvestmentsByType(
                    accessToken,
                    auth -> fundsClient.getInvestments(auth),
                    (auth, id) -> fundsClient.getInvestmentDetail(auth, id),
                    (auth, id) -> fundsClient.getInvestmentTransactions(auth, id),
                    "Funds"
            );
            log.info(">>> FUNDS RESULT: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error(">>> FAILED TO FETCH FUNDS", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchTreasuryTitles(String accessToken) {
        try {
            log.info(">>> FETCHING TREASURY TITLES");
            List<InvestmentData> result = fetchInvestmentsByType(
                    accessToken,
                    auth -> treasuryTitlesClient.getInvestments(auth),
                    (auth, id) -> treasuryTitlesClient.getInvestmentDetail(auth, id),
                    (auth, id) -> treasuryTitlesClient.getInvestmentTransactions(auth, id),
                    "Treasury Titles"
            );
            log.info(">>> TREASURY TITLES RESULT: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error(">>> FAILED TO FETCH TREASURY TITLES", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchVariableIncomes(String accessToken) {
        try {
            log.info(">>> FETCHING VARIABLE INCOMES");
            List<InvestmentData> result = fetchInvestmentsByType(
                    accessToken,
                    auth -> variableIncomesClient.getInvestments(auth),
                    (auth, id) -> variableIncomesClient.getInvestmentDetail(auth, id),
                    (auth, id) -> variableIncomesClient.getInvestmentTransactions(auth, id),
                    "Variable Incomes"
            );
            log.info(">>> VARIABLE INCOMES RESULT: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error(">>> FAILED TO FETCH VARIABLE INCOMES", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchInvestmentsByType(
            String accessToken,
            java.util.function.Function<String, String> listFunction,
            java.util.function.BiFunction<String, String, String> detailFunction,
            java.util.function.BiFunction<String, String, String> transactionsFunction,
            String typeName) throws Exception {

        // Step 1: Get list of investments (contains only IDs)
        String listJws = listFunction.apply("Bearer " + accessToken);
        String listPayload = jwsVerificationService.verifyAndExtractPayload(listJws);
        List<String> investmentIds = parseInvestmentIds(listPayload);

        log.info("Found {} {} investments, fetching details...", investmentIds.size(), typeName);

        // Step 2: Fetch details for each investment
        List<InvestmentData> investments = investmentIds.stream()
                .map(id -> fetchInvestmentDetail(accessToken, detailFunction, transactionsFunction, id))
                .filter(java.util.Objects::nonNull)
                .toList();

        log.info("Successfully fetched {} {} investments", investments.size(), typeName);
        return investments;
    }

    private List<String> parseInvestmentIds(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode dataNode = root.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            log.warn("No investment data found in response");
            return List.of();
        }

        List<String> ids = new java.util.ArrayList<>();
        for (JsonNode node : dataNode) {
            JsonNode idNode = node.get("investmentId");
            if (idNode != null) {
                ids.add(idNode.asText());
            }
        }
        return ids;
    }

    private InvestmentData fetchInvestmentDetail(
            String accessToken,
            java.util.function.BiFunction<String, String, String> detailFunction,
            java.util.function.BiFunction<String, String, String> transactionsFunction,
            String investmentId) {
        try {
            log.debug("Fetching detail for investment: {}", investmentId);

            // Call detail endpoint
            String detailJws = detailFunction.apply("Bearer " + accessToken, investmentId);
            String detailPayload = jwsVerificationService.verifyAndExtractPayload(detailJws);

            // Parse detail response
            JsonNode root = objectMapper.readTree(detailPayload);
            JsonNode dataNode = root.get("data");

            if (dataNode == null) {
                log.warn("No data in detail response for investment: {}", investmentId);
                return null;
            }

            OFBInvestmentDto dto = objectMapper.convertValue(dataNode, OFBInvestmentDto.class);
            InvestmentData data = investmentMapper.toInvestmentData(dto);

            // Fetch and enrich with transaction data
            enrichWithTransactionData(data, accessToken, transactionsFunction, investmentId);

            return data;

        } catch (Exception e) {
            log.error("Failed to fetch detail for investment: {}", investmentId, e);
            return null;
        }
    }

    private void enrichWithTransactionData(
            InvestmentData data,
            String accessToken,
            java.util.function.BiFunction<String, String, String> transactionsFunction,
            String investmentId) {
        try {
            log.debug("Fetching transactions for investment: {}", investmentId);

            // Call transactions endpoint
            String transactionsJws = transactionsFunction.apply("Bearer " + accessToken, investmentId);
            String transactionsPayload = jwsVerificationService.verifyAndExtractPayload(transactionsJws);

            // Parse transactions response
            JsonNode root = objectMapper.readTree(transactionsPayload);
            JsonNode transactionsNode = root.get("data");

            if (transactionsNode == null || !transactionsNode.isArray()) {
                log.debug("No transactions found for investment: {}", investmentId);
                data.setTransactionCount(0);
                return;
            }

            // Count transactions
            data.setTransactionCount(transactionsNode.size());

            // Find first and last transaction dates
            java.time.LocalDate firstDate = null;
            java.time.LocalDate lastDate = null;

            for (JsonNode txn : transactionsNode) {
                JsonNode dateNode = txn.get("transactionDate");
                if (dateNode != null) {
                    java.time.LocalDate date = java.time.LocalDate.parse(dateNode.asText());
                    if (firstDate == null || date.isBefore(firstDate)) {
                        firstDate = date;
                    }
                    if (lastDate == null || date.isAfter(lastDate)) {
                        lastDate = date;
                    }
                }
            }

            data.setFirstTransactionDate(firstDate);
            data.setLastTransactionDate(lastDate);

            log.debug("Enriched investment {} with {} transactions (first: {}, last: {})",
                    investmentId, data.getTransactionCount(), firstDate, lastDate);

        } catch (Exception e) {
            log.warn("Failed to fetch transactions for investment {}: {}", investmentId, e.getMessage());
            data.setTransactionCount(0);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class InvestmentData {
        private String investmentId;
        private String type;
        private String issuerName;
        private Double investedAmount;
        private Double currentValue;
        private Double profitability;
        private String maturityDate;

        // Transaction data for frequency scoring (THE SPEC requirement)
        private Integer transactionCount;
        private java.time.LocalDate firstTransactionDate;
        private java.time.LocalDate lastTransactionDate;
    }
}
