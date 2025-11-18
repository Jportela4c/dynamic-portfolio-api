package com.portfolio.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ofb.provider")
public class OFBProviderProperties {

    private String baseUrl;
    private String clientId;
    private String redirectUri;
    private String scope;

    private Keystore keystore = new Keystore();
    private Truststore truststore = new Truststore();
    private Token token = new Token();
    private Jwks jwks = new Jwks();
    private Connection connection = new Connection();

    @Data
    public static class Keystore {
        private String path;
        private String password;
    }

    @Data
    public static class Truststore {
        private String path;
    }

    @Data
    public static class Token {
        private int cacheTtlSeconds = 3000;
    }

    @Data
    public static class Jwks {
        private int cacheTtlSeconds = 86400;
    }

    @Data
    public static class Connection {
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
    }
}
