package com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M;

import com.swisscom.travelmate.engine.shared.external.authorization.AbstractAuthorizationService;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationTokenClientConfig;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtRequest;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtResponse;
import com.swisscom.travelmate.engine.shared.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecureM2MJwtService extends AbstractAuthorizationService {
    @Autowired
    public SecureM2MJwtClient secureM2MJwtClient;

    @Override
    protected String fetchNewToken(AuthorizationTokenClientConfig config) {
        AuthorizationJwtRequest request = new AuthorizationJwtRequest(
                config.clientSecret,
                config.clientId,
                config.grantType
        );

        ResponseEntity<AuthorizationJwtResponse> response = secureM2MJwtClient.getJwt(request);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Objects.requireNonNull(response.getBody()).access_token;
        } else {
            throw new RuntimeException("Failed to fetch JWT via Body Auth");
        }
    }

    public String getTokenWithBearerPrefix(AuthorizationTokenClientConfig clientConfig) {
        return "Bearer " + getToken(clientConfig);
    }
}