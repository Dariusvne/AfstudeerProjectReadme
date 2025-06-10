package com.swisscom.travelmate.engine.shared.external.authorization.oauth2;

import com.swisscom.travelmate.engine.shared.external.authorization.AbstractAuthorizationService;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationTokenClientConfig;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtResponse;
import com.swisscom.travelmate.engine.shared.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OAuth2JwtService extends AbstractAuthorizationService {
    @Autowired
    public OAuth2JwtClient OAuth2JwtClient;

    @Override
    protected String fetchNewToken(AuthorizationTokenClientConfig config) {
        String credentials = config.clientId + ":" + config.clientSecret;
        String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String authHeader = "Basic " + encoded;

        ResponseEntity<AuthorizationJwtResponse> response = OAuth2JwtClient.getJwt(config.grantType, authHeader);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(response.getBody()).access_token;
        } else {
            throw new RuntimeException("Failed to fetch JWT via Basic Auth");
        }
    }

    @Override
    protected boolean shouldExtractClaims() {
        return false;
    }

    public String getTokenWithBearerPrefix(AuthorizationTokenClientConfig clientConfig) {
        return "Bearer " + getToken(clientConfig);
    }


}
