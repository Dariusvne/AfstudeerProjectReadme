package com.swisscom.travelmate.engine.ServiceTests;

import com.nimbusds.jwt.JWTClaimsSet;
import com.swisscom.travelmate.engine.shared.external.AuthStyle;
import com.swisscom.travelmate.engine.shared.external.authorization.*;
import com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M.SecureM2MJwtClient;
import com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M.SecureM2MJwtService;
import com.swisscom.travelmate.engine.shared.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecureM2MJwtServiceTests {

    private SecureM2MJwtService service;
    private SecureM2MJwtClient mockClient;
    private JwtService mockJwtService;

    @BeforeEach
    void setUp() {
        service = new SecureM2MJwtService();

        mockClient = mock(SecureM2MJwtClient.class);
        mockJwtService = mock(JwtService.class);

        service.secureM2MJwtClient = mockClient;
        service.jwtService = mockJwtService;
    }

    @Test
    void getToken_fetchesAndCachesToken() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);
        config.grantType = "client_credentials";

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "token-body";

        ResponseEntity<AuthorizationJwtResponse> response = new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        doReturn(response).when(mockClient).getJwt(any(AuthorizationJwtRequest.class));
        when(mockJwtService.isExpired(any())).thenReturn(false);
        when(mockJwtService.getClaimsSetWithoutVerification("token-body"))
                .thenReturn(new JWTClaimsSet.Builder().build());

        String token1 = service.getToken(config);
        String token2 = service.getToken(config);

        assertEquals("token-body", token1);
        assertSame(token1, token2);
        verify(mockClient, times(1)).getJwt(any());
    }

    @Test
    void getToken_throwsException_whenJwtFetchFails() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);
        config.grantType = "client_credentials";

        ResponseEntity<AuthorizationJwtResponse> failedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        doReturn(failedResponse).when(mockClient).getJwt(any(AuthorizationJwtRequest.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.getToken(config);
        });

        assertEquals("Failed to fetch JWT via Body Auth", ex.getMessage());
    }

    @Test
    void getToken_reusesCachedTokenIfNotExpired() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);
        config.grantType = "client_credentials";

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "cached-token";

        ResponseEntity<AuthorizationJwtResponse> response = new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        doReturn(response).when(mockClient).getJwt(any(AuthorizationJwtRequest.class));
        when(mockJwtService.isExpired(any())).thenReturn(false);
        when(mockJwtService.getClaimsSetWithoutVerification("cached-token"))
                .thenReturn(new JWTClaimsSet.Builder().build());

        String token1 = service.getToken(config);
        String token2 = service.getToken(config);

        assertEquals("cached-token", token1);
        assertSame(token1, token2);
        verify(mockClient, times(1)).getJwt(any());
    }

    @Test
    void getTokenWithBearerPrefix_returnsCorrectlyPrefixedToken() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);
        config.grantType = "client_credentials";

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "body-jwt";

        ResponseEntity<AuthorizationJwtResponse> response = new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        doReturn(response).when(mockClient).getJwt(any(AuthorizationJwtRequest.class));
        when(mockJwtService.isExpired(any())).thenReturn(false);

        String bearerToken = service.getTokenWithBearerPrefix(config);

        assertEquals("Bearer body-jwt", bearerToken);
    }
}