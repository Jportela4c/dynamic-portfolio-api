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

    private final OFBInvestmentClient investmentClient;
    private final OFBOAuth2ClientService oAuth2ClientService;
    private final JWSVerificationService jwsVerificationService;
    private final ObjectMapper objectMapper;
    private final OFBInvestmentMapper investmentMapper;

    public List<InvestmentData> fetchInvestments() throws Exception {
        log.info("Fetching investment data from OFB provider");

        // Get access token
        String accessToken = oAuth2ClientService.getAccessToken();

        // Call investments API using HTTP Interface client
        String jwsToken = investmentClient.getInvestments("Bearer " + accessToken);

        // Verify JWS signature and extract payload
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

        List<OFBInvestmentDto> dtos = objectMapper.convertValue(
                dataNode,
                new TypeReference<List<OFBInvestmentDto>>() {}
        );

        return investmentMapper.toInvestmentDataList(dtos);
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
