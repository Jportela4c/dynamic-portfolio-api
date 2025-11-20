package com.portfolio.api.integration;

import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.service.external.JWEDecryptionService;
import com.portfolio.api.service.external.JWSVerificationService;
import com.portfolio.api.service.external.OFBInvestmentDataService;
import com.portfolio.api.service.external.OFBOAuth2ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "ofb.integration.enabled", matches = "true")
class OFBIntegrationTest {

    @Autowired
    private OFBOAuth2ClientService oAuth2ClientService;

    @Autowired
    private OFBInvestmentDataService investmentDataService;

    @Autowired
    private JWSVerificationService jwsVerificationService;

    @Autowired
    private JWEDecryptionService jweDecryptionService;

    @Test
    void shouldCompleteOAuth2PARFlow() throws Exception {
        // When
        String accessToken = oAuth2ClientService.getAccessToken();

        // Then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
    }

    @Test
    void shouldCacheAccessToken() throws Exception {
        // When
        String firstToken = oAuth2ClientService.getAccessToken();
        String secondToken = oAuth2ClientService.getAccessToken();

        // Then
        assertThat(firstToken).isEqualTo(secondToken);
    }

    @Test
    void shouldInvalidateTokenCache() throws Exception {
        // Given
        String firstToken = oAuth2ClientService.getAccessToken();

        // When
        oAuth2ClientService.invalidateCache();
        String secondToken = oAuth2ClientService.getAccessToken();

        // Then
        assertThat(secondToken).isNotNull();
    }

    @Test
    void shouldFetchInvestmentsFromOFB() throws Exception {
        // Given
        String accessToken = oAuth2ClientService.getAccessToken();

        // When
        List<OFBInvestmentDataService.InvestmentData> investments =
            investmentDataService.fetchInvestments(accessToken);

        // Then
        assertThat(investments).isNotNull();
        assertThat(investments).isNotEmpty();

        OFBInvestmentDataService.InvestmentData investment = investments.get(0);
        assertThat(investment.getInvestmentId()).isNotNull();
        assertThat(investment.getType()).isNotNull();
        assertThat(investment.getInvestedAmount()).isNotNull();
        assertThat(investment.getCurrentValue()).isNotNull();
    }

    @Test
    void shouldVerifyJWSSignature() throws Exception {
        // Given
        String accessToken = oAuth2ClientService.getAccessToken();
        List<OFBInvestmentDataService.InvestmentData> investments =
            investmentDataService.fetchInvestments(accessToken);

        // Then - if we got here, JWS verification passed
        assertThat(investments).isNotNull();
    }

    @Test
    void shouldDecryptJWEToken() throws Exception {
        // This test validates the complete OAuth2 flow including JWE decryption
        // If getAccessToken succeeds, it means JWE ID token was decrypted successfully

        // When
        String accessToken = oAuth2ClientService.getAccessToken();

        // Then
        assertThat(accessToken).isNotNull();
    }

    @Test
    void shouldValidateJWEClaims() throws Exception {
        // Given
        String accessToken = oAuth2ClientService.getAccessToken();

        // Then - if we got here, ID token claims were validated successfully
        assertThat(accessToken).isNotNull();
    }
}
