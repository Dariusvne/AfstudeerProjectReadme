package com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M;

import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtRequest;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

public interface SecureM2MJwtClient {
    @PostMapping("/jwt-auth/token")
    public ResponseEntity<AuthorizationJwtResponse> getJwt (AuthorizationJwtRequest request);
}
