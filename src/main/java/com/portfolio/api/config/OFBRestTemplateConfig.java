package com.portfolio.api.config;

import com.portfolio.api.service.external.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OFBRestTemplateConfig {

    private final OFBProviderProperties properties;
    private final ResourceLoader resourceLoader;

    @Bean
    public OFBBankFixedIncomesClient ofbBankFixedIncomesClient(RestClient ofbRestClient) {
        log.info("Creating OFB Bank Fixed Incomes HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBBankFixedIncomesClient.class);
    }

    @Bean
    public OFBCreditFixedIncomesClient ofbCreditFixedIncomesClient(RestClient ofbRestClient) {
        log.info("Creating OFB Credit Fixed Incomes HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBCreditFixedIncomesClient.class);
    }

    @Bean
    public OFBFundsClient ofbFundsClient(RestClient ofbRestClient) {
        log.info("Creating OFB Funds HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBFundsClient.class);
    }

    @Bean
    public OFBTreasuryTitlesClient ofbTreasuryTitlesClient(RestClient ofbRestClient) {
        log.info("Creating OFB Treasury Titles HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBTreasuryTitlesClient.class);
    }

    @Bean
    public OFBVariableIncomesClient ofbVariableIncomesClient(RestClient ofbRestClient) {
        log.info("Creating OFB Variable Incomes HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBVariableIncomesClient.class);
    }

    @Bean
    public OFBOAuth2Client ofbOAuth2Client(RestClient ofbRestClient) {
        log.info("Creating OFB OAuth2 HTTP Interface client");
        RestClientAdapter adapter = RestClientAdapter.create(ofbRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OFBOAuth2Client.class);
    }

    @Bean
    public RestClient ofbRestClient() throws Exception {
        log.info("Configuring OFB RestClient with mTLS");

        SSLContext sslContext = createSSLContext();
        HttpClientConnectionManager connectionManager = createConnectionManager(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnection().getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getConnection().getReadTimeoutMs()));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    private SSLContext createSSLContext() throws Exception {
        log.debug("Loading keystore from: {}", properties.getKeystore().getPath());

        // Load client keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        Resource keystoreResource = resourceLoader.getResource(properties.getKeystore().getPath());
        try (InputStream keystoreStream = keystoreResource.getInputStream()) {
            keyStore.load(keystoreStream, properties.getKeystore().getPassword().toCharArray());
        }

        // Load server certificate for trust
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        Resource trustCertResource = resourceLoader.getResource(properties.getTruststore().getPath());
        try (InputStream certStream = trustCertResource.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(certStream);
            trustStore.setCertificateEntry("ofb-server", cert);
        }

        return SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, properties.getKeystore().getPassword().toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();
    }

    private HttpClientConnectionManager createConnectionManager(SSLContext sslContext) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .build())
                .build();
    }
}
