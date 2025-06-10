package com.swisscom.travelmate.engine.shared.external.authorization;

import com.nimbusds.jwt.JWTClaimsSet;
import com.swisscom.travelmate.engine.shared.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAuthorizationService {
    @Autowired
    public JwtService jwtService;

    protected final Map<String, JWTClaimsSet> tokenClaims = new HashMap<>();
    protected final Map<String, String> tokenString = new HashMap<>();

    public String getToken(AuthorizationTokenClientConfig clientConfig) {
        String token = tokenString.get(clientConfig.clientId);

        if (tokenClaims.get(clientConfig.clientId) == null || jwtService.isExpired(tokenClaims.get(clientConfig.clientId))) {
            token = fetchNewToken(clientConfig);
            tokenString.put(clientConfig.clientId, token);

            if (shouldExtractClaims()) {
                JWTClaimsSet claims = jwtService.getClaimsSetWithoutVerification(token);
                tokenClaims.put(clientConfig.clientId, claims);
            }
        }

        return token;
    }

    protected abstract String fetchNewToken(AuthorizationTokenClientConfig config);
    protected boolean shouldExtractClaims() {
        return true;
    }}
