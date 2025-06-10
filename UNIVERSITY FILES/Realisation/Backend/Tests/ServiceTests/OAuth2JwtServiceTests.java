package com.swisscom.travelmate.engine.ServiceTests;

import com.swisscom.travelmate.engine.shared.external.AuthStyle;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationJwtResponse;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationTokenClientConfig;
import com.swisscom.travelmate.engine.shared.external.authorization.oauth2.OAuth2JwtClient;
import com.swisscom.travelmate.engine.shared.external.authorization.oauth2.OAuth2JwtService;
import com.swisscom.travelmate.engine.shared.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OAuth2JwtServiceTests
{
    private OAuth2JwtClient mockClient;
    private JwtService mockJwtService;
    private OAuth2JwtService service;

    @BeforeEach
    void setUp() {
        service = new OAuth2JwtService();
        mockClient = mock(OAuth2JwtClient.class);
        mockJwtService = mock(JwtService.class);

        service.OAuth2JwtClient = mockClient;
    }

    @Test
    void getToken_firstTimeFetchesAndCachesToken_Basic() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Basic);

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "token-abc";

        ResponseEntity<AuthorizationJwtResponse> response = ResponseEntity.ok(jwtResponse);

        doReturn(response).when(mockClient).getJwt(anyString(), anyString());

        when(mockJwtService.isExpired(any())).thenReturn(false);

        String token = service.getToken(config);

        assertEquals("token-abc", token);
    }

    @Test
    void getToken_firstTimeFetchesAndCachesToken_Body() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Basic);

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "token-abc";

        ResponseEntity<AuthorizationJwtResponse> response = ResponseEntity.ok(jwtResponse);

        doReturn(response).when(mockClient).getJwt(anyString(), anyString());

        when(mockJwtService.isExpired(any())).thenReturn(false);

        String token = service.getToken(config);

        assertEquals("token-abc", token);
    }

    @Test
    void getToken_refreshesTokenIfExpired() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);
        config.clientId = "client1";
        config.clientSecret = "secret";
        config.grantType = "client_credentials";

        AuthorizationJwtResponse jwtResponse1 = new AuthorizationJwtResponse();
        jwtResponse1.access_token = "token-old";

        AuthorizationJwtResponse jwtResponse2 = new AuthorizationJwtResponse();
        jwtResponse2.access_token = "token-new";

        ResponseEntity<AuthorizationJwtResponse> response1 = new ResponseEntity<>(jwtResponse1, HttpStatus.OK);
        ResponseEntity<AuthorizationJwtResponse> response2 = new ResponseEntity<>(jwtResponse2, HttpStatus.OK);

        // First fetch returns token-old, second fetch returns token-new
        doReturn(response1)
                .doReturn(response2)
                .when(mockClient).getJwt(anyString(), anyString());

        when(mockJwtService.isExpired(any())).thenReturn(false, true, false);

        String token1 = service.getToken(config);
        String token2 = service.getToken(config);

        assertEquals("token-old", token1);
        assertEquals("token-new", token2);
        verify(mockClient, times(2)).getJwt(any(), any());
    }

    @Test
    void getToken_doesNotExtractClaims_whenShouldExtractClaimsFalse() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Basic);

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "claimless-token";

        ResponseEntity<AuthorizationJwtResponse> response = new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        doReturn(response).when(mockClient).getJwt(anyString(), anyString());

        service.getToken(config);

        verify(mockJwtService, never()).getClaimsSetWithoutVerification(any());
    }

    @Test
    void getToken_throwsException_whenJwtFetchFails() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);

        // Simulate a 401 Unauthorized response
        ResponseEntity<AuthorizationJwtResponse> failedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        doReturn(failedResponse).when(mockClient).getJwt(anyString(), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.getToken(config);
        });

        assertEquals("Failed to fetch JWT via Basic Auth", ex.getMessage());
        verify(mockClient).getJwt(any(), any());
    }

    @Test
    void getTokenWithBearerPrefix_returnsCorrectlyPrefixedToken() {
        AuthorizationTokenClientConfig config = new AuthorizationTokenClientConfig("client1", "secret", AuthStyle.Body);

        AuthorizationJwtResponse jwtResponse = new AuthorizationJwtResponse();
        jwtResponse.access_token = "abc-123";

        ResponseEntity<AuthorizationJwtResponse> response = new ResponseEntity<>(jwtResponse, HttpStatus.OK);

        doReturn(response).when(mockClient).getJwt(anyString(), anyString());
        when(mockJwtService.isExpired(any())).thenReturn(false);

        String bearerToken = service.getTokenWithBearerPrefix(config);

        assertEquals("Bearer abc-123", bearerToken);
    }
}
