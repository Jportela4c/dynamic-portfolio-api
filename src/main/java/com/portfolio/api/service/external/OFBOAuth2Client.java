package com.portfolio.api.service.external;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Declarative HTTP client for OFB OAuth2 API.
 */
@HttpExchange("/oauth2")
public interface OFBOAuth2Client {

    @PostExchange("/par")
    String pushAuthorizationRequest(@RequestBody MultiValueMap<String, String> request);

    @GetExchange("/authorize")
    String authorize(@RequestParam("request_uri") String requestUri,
                     @RequestParam("client_id") String clientId);

    @PostExchange("/token")
    String exchangeToken(@RequestBody MultiValueMap<String, String> request);

    @GetExchange("/jwks")
    String getJwks();
}
