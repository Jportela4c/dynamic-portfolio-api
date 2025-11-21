package com.portfolio.api.config;

import com.portfolio.api.provider.InvestmentPlatformProvider;
import com.portfolio.api.provider.OFBAuthProvider;
import com.portfolio.api.provider.dto.CustomerPortfolio;
import com.portfolio.api.provider.dto.Investment;
import com.portfolio.api.provider.dto.Position;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Test configuration providing mock implementations of external providers.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Mock OFB auth provider that returns a dummy token.
     */
    @Bean
    @Primary
    public OFBAuthProvider testOFBAuthProvider() {
        return customerId -> "test-token-for-customer-" + customerId;
    }

    /**
     * Mock investment platform provider that returns empty data.
     */
    @Bean
    @Primary
    public InvestmentPlatformProvider testInvestmentPlatformProvider() {
        return new InvestmentPlatformProvider() {
            @Override
            public CustomerPortfolio getPortfolio(String cpf) {
                return CustomerPortfolio.builder()
                        .cpf(cpf)
                        .totalInvestido(BigDecimal.ZERO)
                        .valorAtual(BigDecimal.ZERO)
                        .rentabilidadeTotal(BigDecimal.ZERO)
                        .dataUltimaAtualizacao(LocalDateTime.now())
                        .build();
            }

            @Override
            public List<Investment> getInvestmentHistory(String cpf) {
                return Collections.emptyList();
            }

            @Override
            public List<Position> getCurrentPositions(String cpf) {
                return Collections.emptyList();
            }
        };
    }
}
