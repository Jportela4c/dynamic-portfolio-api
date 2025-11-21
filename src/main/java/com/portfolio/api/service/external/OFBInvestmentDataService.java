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
            log.debug("Fetching bank fixed incomes");
            return fetchInvestmentsByType(
                    accessToken,
                    auth -> bankFixedIncomesClient.getInvestments(auth),
                    (auth, id) -> bankFixedIncomesClient.getInvestmentDetail(auth, id),
                    "Bank Fixed Incomes"
            );
        } catch (Exception e) {
            log.error("Failed to fetch bank fixed incomes", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchCreditFixedIncomes(String accessToken) {
        try {
            log.debug("Fetching credit fixed incomes");
            return fetchInvestmentsByType(
                    accessToken,
                    auth -> creditFixedIncomesClient.getInvestments(auth),
                    (auth, id) -> creditFixedIncomesClient.getInvestmentDetail(auth, id),
                    "Credit Fixed Incomes"
            );
        } catch (Exception e) {
            log.error("Failed to fetch credit fixed incomes", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchFunds(String accessToken) {
        try {
            log.debug("Fetching funds");
            return fetchInvestmentsByType(
                    accessToken,
                    auth -> fundsClient.getInvestments(auth),
                    (auth, id) -> fundsClient.getInvestmentDetail(auth, id),
                    "Funds"
            );
        } catch (Exception e) {
            log.error("Failed to fetch funds", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchTreasuryTitles(String accessToken) {
        try {
            log.debug("Fetching treasury titles");
            return fetchInvestmentsByType(
                    accessToken,
                    auth -> treasuryTitlesClient.getInvestments(auth),
                    (auth, id) -> treasuryTitlesClient.getInvestmentDetail(auth, id),
                    "Treasury Titles"
            );
        } catch (Exception e) {
            log.error("Failed to fetch treasury titles", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchVariableIncomes(String accessToken) {
        try {
            log.debug("Fetching variable incomes");
            return fetchInvestmentsByType(
                    accessToken,
                    auth -> variableIncomesClient.getInvestments(auth),
                    (auth, id) -> variableIncomesClient.getInvestmentDetail(auth, id),
                    "Variable Incomes"
            );
        } catch (Exception e) {
            log.error("Failed to fetch variable incomes", e);
            return List.of();
        }
    }

    private List<InvestmentData> fetchInvestmentsByType(
            String accessToken,
            java.util.function.Function<String, String> listFunction,
            java.util.function.BiFunction<String, String, String> detailFunction,
            String typeName) throws Exception {

        // Step 1: Get list of investments (contains only IDs)
        String listJws = listFunction.apply("Bearer " + accessToken);
        String listPayload = jwsVerificationService.verifyAndExtractPayload(listJws);
        List<String> investmentIds = parseInvestmentIds(listPayload);

        log.info("Found {} {} investments, fetching details...", investmentIds.size(), typeName);

        // Step 2: Fetch details for each investment
        List<InvestmentData> investments = investmentIds.stream()
                .map(id -> fetchInvestmentDetail(accessToken, detailFunction, id))
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
            return investmentMapper.toInvestmentData(dto);

        } catch (Exception e) {
            log.error("Failed to fetch detail for investment: {}", investmentId, e);
            return null;
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
    }
}
