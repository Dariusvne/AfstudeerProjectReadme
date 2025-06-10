package com.swisscom.travelmate.engine.ServiceTests;


import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.authorization.oauth2.OAuth2JwtService;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.client.WFIClient;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WfiServiceTests {

    @InjectMocks
    private WFIService wfiService;

    @Mock
    private OAuth2JwtService oauth2JwtService;

    @Mock
    private WFIClient wfiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject test properties manually since @Value is not processed in unit tests
        wfiService = new WFIService();
        ReflectionTestUtils.setField(wfiService, "secureM2MJwtService", oauth2JwtService);
        ReflectionTestUtils.setField(wfiService, "wfiClient", wfiClient);
        ReflectionTestUtils.setField(wfiService, "wfiClientId", "test-client-id");
        ReflectionTestUtils.setField(wfiService, "wfiClientSecret", "test-secret");
    }

    private WFIUserDto createTestUserDto(String empNr, String email) {
        return WFIUserDto.builder()
                .id("abc123")
                .rev("1")
                .orgCompany("Swisscom AG")
                .orgLineManager("mgr001")
                .orgOUName("Tech Department")
                .personalNumber(empNr)
                .preferredLanguage("en")
                .displayName("John Doe")
                .firstName("John")
                .lastName("Doe")
                .adrSwisscomCountry("CH")
                .mailInternal(email)
                .orgLineManagerName("Jane Manager")
                .build();
    }

    private WFIUserResponseDto createResponseWithUser(WFIUserDto user) {
        WFIUserResponseDto response = new WFIUserResponseDto();
        response.setResult(List.of(user));
        response.setResultCount(1);
        response.setTotalPagedResultsPolicy("EXACT");
        response.setTotalPagedResults(1);
        response.setRemainingPagedResults(0);
        return response;
    }

    @Test
    void testGetUserByEmployeeNumber_returnsCorrectUser() {
        String empNr = "EMP123";
        String token = "Bearer token";
        WFIUserDto user = createTestUserDto(empNr, "john.doe@swisscom.com");
        WFIUserResponseDto response = createResponseWithUser(user);
        String expectedQuery = "personalnumber eq \"" + empNr + "\"";

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(response))
                .when(wfiClient)
                .queryUsers(eq(expectedQuery), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByEmployeeNumber(empNr);

        assertTrue(result.isPresent());
        assertEquals("john.doe@swisscom.com", result.get().getMailInternal());
    }

    @Test
    void testGetUserByEmail_returnsUser() {
        String email = "john.doe@swisscom.com";
        String token = "Bearer token";
        WFIUserDto user = createTestUserDto("EMP123", email);
        WFIUserResponseDto response = createResponseWithUser(user);
        String expectedQuery = "mailInternal eq \"" + email + "\"";

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(response))
                .when(wfiClient)
                .queryUsers(eq(expectedQuery), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getMailInternal());
    }

    @Test
    void testGetUserByFirstAndLastName_returnsUser() {
        String token = "Bearer token";
        WFIUserDto user = createTestUserDto("EMP456", "john.doe@swisscom.com");
        WFIUserResponseDto response = createResponseWithUser(user);
        String expectedQuery = "(firstName eq \"John\" and lastName eq \"Doe\")";

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(response))
                .when(wfiClient)
                .queryUsers(eq(expectedQuery), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByFirstAndLastName("John", "Doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void testGetUsersByEmployeeNumbers_returnsResponse() {
        String token = "Bearer token";
        List<String> empNumbers = List.of("EMP001", "EMP002");
        WFIUserDto user1 = createTestUserDto("EMP001", "a@a.com");
        WFIUserDto user2 = createTestUserDto("EMP002", "b@b.com");
        WFIUserResponseDto response = new WFIUserResponseDto();
        response.setResult(List.of(user1, user2));
        response.setResultCount(2);

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(response))
                .when(wfiClient)
                .queryUsers(argThat(q -> q.contains("personalnumber")), eq(token), eq(1));

        WFIUserResponseDto result = wfiService.getUsersByEmployeeNumbers(empNumbers);

        assertEquals(2, result.getResult().size());
    }

    @Test
    void testGetUserByEmail_whenNotFound_returnsEmpty() {
        String email = "not.exists@swisscom.com";
        String token = "Bearer token";
        WFIUserResponseDto emptyResponse = new WFIUserResponseDto();
        emptyResponse.setResult(List.of());

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(emptyResponse))
                .when(wfiClient)
                .queryUsers(eq("mailInternal eq \"" + email + "\""), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByEmail(email);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUsersByName_returnsResponse() {
        String token = "Bearer token";
        AgileMembershipsDto member1 = AgileMembershipsDto.builder().build();
        member1.firstName = "Alice";
        member1.lastName = "Smith";
        AgileMembershipsDto member2 = AgileMembershipsDto.builder().build();
        member2.firstName = "Bob";
        member2.lastName = "Brown";

        List<AgileMembershipsDto> members = List.of(member1, member2);

        WFIUserDto user1 = createTestUserDto("EMP001", "alice.smith@swisscom.com");
        WFIUserDto user2 = createTestUserDto("EMP002", "bob.brown@swisscom.com");
        WFIUserResponseDto response = new WFIUserResponseDto();
        response.setResult(List.of(user1, user2));
        response.setResultCount(2);

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(response))
                .when(wfiClient)
                .queryUsers(argThat(q -> q.contains("firstName eq")), eq(token), eq(1));

        WFIUserResponseDto result = wfiService.getUsersByName(members);

        assertNotNull(result);
        assertEquals(2, result.getResult().size());
    }

    @Test
    void testGetUserByFirstAndLastName_whenNotFound_returnsEmpty() {
        String token = "Bearer token";
        String query = "(firstName eq \"Ghost\" and lastName eq \"User\")";

        WFIUserResponseDto emptyResponse = new WFIUserResponseDto();
        emptyResponse.setResult(List.of()); // Important: non-null but empty

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(emptyResponse))
                .when(wfiClient)
                .queryUsers(eq(query), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByFirstAndLastName("Ghost", "User");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserByOnlyOneName_whenNotFound_returnsEmpty() {
        String fullName = "Ghost User";
        String first = "Ghost";
        String last = "User";
        String token = "Bearer token";
        String expectedQuery = "(displayName co \"" + first + "\" and displayName co \"" + last + "\")";

        WFIUserResponseDto emptyResponse = new WFIUserResponseDto();
        emptyResponse.setResult(List.of()); // Important: non-null but empty

        when(oauth2JwtService.getTokenWithBearerPrefix(any())).thenReturn(token);
        doReturn(ResponseEntity.ok(emptyResponse))
                .when(wfiClient)
                .queryUsers(eq(expectedQuery), eq(token), eq(1));

        Optional<WFIUserDto> result = wfiService.getUserByOnlyOneName(fullName);

        assertTrue(result.isEmpty());
    }

}