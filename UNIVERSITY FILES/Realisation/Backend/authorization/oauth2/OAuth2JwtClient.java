package com.swisscom.travelmate.engine.shared.external.authorization.oauth2;

import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface OAuth2JwtClient {
    @PostMapping("/oauth2/token")
    public ResponseEntity<AuthorizationJwtResponse> getJwt(
            @RequestParam("grant_type") String grantType,
            @RequestHeader("Authorization") String authorization
    );
}