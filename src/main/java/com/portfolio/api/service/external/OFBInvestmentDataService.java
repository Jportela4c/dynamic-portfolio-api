package com.portfolio.api.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.config.OFBProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OFBInvestmentDataService {

    @Qualifier("ofbRestTemplate")
    private final RestTemplate restTemplate;
    private final OFBProviderProperties properties;
    private final OFBOAuth2ClientService oAuth2ClientService;
    private final JWSVerificationService jwsVerificationService;
    private final ObjectMapper objectMapper;

    public List<InvestmentData> fetchInvestments() throws Exception {
        log.info("Fetching investment data from OFB provider");

        // Get access token
        String accessToken = oAuth2ClientService.getAccessToken();

        // Call investments API
        String investmentsEndpoint = properties.getBaseUrl() + "/investments/v1/investments";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                investmentsEndpoint,
                HttpMethod.GET,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Investments API call failed with status: " + response.getStatusCode());
        }

        // Verify JWS signature and extract payload
        String jwsToken = response.getBody();
        String payload = jwsVerificationService.verifyAndExtractPayload(jwsToken);

        // Parse investment data
        List<InvestmentData> investments = parseInvestmentData(payload);

        log.info("Successfully fetched {} investments from OFB provider", investments.size());
        return investments;
    }

    private List<InvestmentData> parseInvestmentData(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode dataNode = root.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            log.warn("No investment data found in response");
            return List.of();
        }

        List<InvestmentData> investments = new ArrayList<>();

        for (JsonNode investmentNode : dataNode) {
            InvestmentData investment = InvestmentData.builder()
                    .investmentId(investmentNode.get("investmentId").asText())
                    .type(investmentNode.get("type").asText())
                    .issuerName(investmentNode.path("issuerName").asText(null))
                    .investedAmount(investmentNode.path("investedAmount").asDouble(0.0))
                    .currentValue(investmentNode.path("currentValue").asDouble(0.0))
                    .profitability(investmentNode.path("profitability").asDouble(0.0))
                    .maturityDate(investmentNode.path("maturityDate").asText(null))
                    .build();

            investments.add(investment);
        }

        return investments;
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
