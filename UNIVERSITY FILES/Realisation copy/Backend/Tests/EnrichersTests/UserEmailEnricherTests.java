package com.swisscom.travelmate.engine.EnrichersTests;

import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.enrichers.custom.email.UserEmailEnricher;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserEmailEnricherTests {
    private WFIService wfiService;
    private UserEmailEnricher enricher;
    private WFIUserDto wfiUserDto;

    @BeforeEach
    void setUp() {
        wfiService = mock(WFIService.class);
        enricher = new UserEmailEnricher(wfiService);

        wfiUserDto = WFIUserDto.builder()
                .orgLineManager("manager-456")
                .orgOUName("SUB-SDC-NL-DOS-DVX")
                .personalNumber("123456")
                .preferredLanguage("en")
                .displayName("John Doe")
                .firstName("John")
                .lastName("Doe")
                .mailInternal("user@example.com")
                .orgLineManagerName("Jane Manager")
                .build();
    }

    @Test
    void enrich_setsEmailWhenMissingAndWfiUserExists() {
        User user = UserTestUtils.createUser("John", "Doe");
        user.setEmployeeNr("123456");
        user.setEmail(null);

        when(wfiService.getUserByEmployeeNumber("123456")).thenReturn(Optional.of(wfiUserDto));

        User enriched = enricher.enrich(user);

        assertEquals("user@example.com", enriched.getEmail());
    }

    @Test
    void enrich_doesNotChangeEmailWhenAlreadySet() {
        User user = UserTestUtils.createUser("John", "Doe");
        user.setEmployeeNr("123456");
        user.setEmail("existing@example.com");

        when(wfiService.getUserByEmployeeNumber("123456")).thenReturn(Optional.of(wfiUserDto));

        User enriched = enricher.enrich(user);

        assertEquals("existing@example.com", enriched.getEmail());
    }

    @Test
    void enrich_leavesEmailNullWhenNoWfiUserFound() {
        User user = UserTestUtils.createUser("John", "Doe");
        user.setEmployeeNr("123456");
        user.setEmail(null);

        when(wfiService.getUserByEmployeeNumber("123456")).thenReturn(Optional.empty());

        User enriched = enricher.enrich(user);

        assertNull(enriched.getEmail());
    }
}
