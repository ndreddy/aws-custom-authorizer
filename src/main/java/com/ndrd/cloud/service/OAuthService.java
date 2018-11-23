package com.ndrd.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


public class OAuthService implements AuthorizerService {
    public static String TOKEN_API = System.getenv("TOKEN_API");
    static final Logger log = LoggerFactory.getLogger(OAuthService.class);

    @Override
    public String authenticateUser(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TOKEN_API)
                .queryParam("grant_type", "password")
                .queryParam("username", username)
                .queryParam("password", password);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        log.debug("Making GET " + builder.toUriString());
        ResponseEntity<String> res = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);
        log.debug("GET Response : " + res.getStatusCode());
        if (res.getStatusCode() != HttpStatus.OK) {
            throw new HttpClientErrorException(res.getStatusCode());
        }

        return res.getBody();
    }

    @Override
    public String authenticateToken(String accessToken) {
        return null;
    }
}
