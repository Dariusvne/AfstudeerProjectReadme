package com.swisscom.travelmate.engine.EnrichersTests;

import com.swisscom.travelmate.engine.shared.enrichers.custom.email.PeopleGroupsAgileMembershipsEmailEnricher;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PeopleGroupsAgileMembershipsEmailEnricherTests {

    private WFIService wfiService;
    private PeopleGroupsAgileMembershipsEmailEnricher enricher;
    private WFIUserDto wfiUserDto;

    @BeforeEach
    void setup() {
        wfiService = mock(WFIService.class);
        enricher = new PeopleGroupsAgileMembershipsEmailEnricher(wfiService);

        wfiUserDto = WFIUserDto.builder()
                .orgLineManager("manager-456")
                .orgOUName("SUB-SDC-NL-DOS-DVX")
                .personalNumber("123456")
                .preferredLanguage("en")
                .displayName("John Doe")
                .firstName("John")
                .lastName("Doe")
                .mailInternal("john.doe@swisscom.com")
                .orgLineManagerName("Jane Manager")
                .build();
    }

    @Test
    void testEnrich_whenWfiUserFound_setsEmail() {
        PeopleGroupsAgileMembershipsDto dto = PeopleGroupsAgileMembershipsDto.builder().employeeNr(123456).roleId(17).roleName("Product Owner").build();

        WFIUserResponseDto response = new WFIUserResponseDto();
        response.setResult(List.of(wfiUserDto));

        when(wfiService.getUsersByEmployeeNumbers(List.of("123456")))
                .thenReturn(response);

        List<PeopleGroupsAgileMembershipsDto> enriched = enricher.enrich(List.of(dto));

        assertEquals("john.doe@swisscom.com", enriched.get(0).getEmail());
    }

    @Test
    void testEnrich_whenNoMatchingWfiUser_emailRemainsUnset() {
        PeopleGroupsAgileMembershipsDto dto = PeopleGroupsAgileMembershipsDto.builder().employeeNr(7115205).roleId(17).roleName("Product Owner").build();

        WFIUserDto unrelatedUser = WFIUserDto.builder()
                .personalNumber("0000000")
                .mailInternal("unrelated@example.com")
                .build();

        WFIUserResponseDto response = new WFIUserResponseDto();
        response.setResult(List.of(unrelatedUser));

        when(wfiService.getUsersByEmployeeNumbers(any()))
                .thenReturn(response);

        List<PeopleGroupsAgileMembershipsDto> enriched = enricher.enrich(List.of(dto));

        assertNull(enriched.get(0).getEmail());
    }

    @Test
    void testEnrich_withEmptyInput_returnsEmptyList() {
        List<PeopleGroupsAgileMembershipsDto> enriched = enricher.enrich(List.of());
        assertTrue(enriched.isEmpty());
    }

}