package com.swisscom.travelmate.engine.EnrichersTests;

import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.enrichers.custom.email.ScaffoldedUserEnricher;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScaffoldedUserEnricherTests {

    private WFIService wfiService;
    private ScaffoldedUserEnricher enricher;
    private WFIUserDto wfiUserDto;

    @BeforeEach
    void setup() {
        wfiService = mock(WFIService.class);
        enricher = new ScaffoldedUserEnricher(wfiService);

        wfiUserDto = WFIUserDto.builder()
                .orgLineManager("manager-456")
                .orgOUName("SUB-SDC-NL-DOS-DVX")
                .personalNumber("PN7890")
                .preferredLanguage("en")
                .displayName("John Doe")
                .firstName("John")
                .lastName("Doe")
                .mailInternal("john.doe@swisscom.com")
                .orgLineManagerName("Jane Manager")
                .build();
    }

    @Test
    void testEnrich_whenUserIsScaffolded_andWfiUserFound() {
        User user = User.createScaffold("123456");

        when(wfiService.getUserByEmployeeNumber("123456"))
                .thenReturn(Optional.of(wfiUserDto));

        User enriched = enricher.enrich(user);

        assertEquals(wfiUserDto.getFirstName(), enriched.getFirstName());
        assertEquals(wfiUserDto.getLastName(), enriched.getLastName());
        assertEquals(wfiUserDto.getOrgOUName(), enriched.getDepartmentName());
        assertEquals(Country.NL, enriched.getHomeCountry()); // Assuming UserUtils works
    }

    @Test
    void testEnrich_whenNoWfiUserFound_returnsEmpty() {
        User user = User.createScaffold("123456");

        when(wfiService.getUserByEmployeeNumber("123456"))
                .thenReturn(Optional.empty());

        User enriched = enricher.enrich(user);

        assertTrue(enriched.isScaffolded());
    }

    @Test
    void testEnrich_whenUserIsNotScaffolded_returnsSameUser() {
        User user = UserTestUtils.createUser("Darius", "van Essen");
        user.setEmployeeNr("654321");

        when(wfiService.getUserByEmployeeNumber("654321"))
                .thenReturn(Optional.of(wfiUserDto));

        User enriched = enricher.enrich(user);

        assertSame(user, enriched); // No changes should have been applied
    }
}