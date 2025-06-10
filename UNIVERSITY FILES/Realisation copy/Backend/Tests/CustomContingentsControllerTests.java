package com.swisscom.travelmate.engine.ControllerTests;


import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelDetailsDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import com.swisscom.travelmate.engine.modules.user.dto.UserGroupDto;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.custom.TravelRequestCompactedDto;
import com.swisscom.travelmate.engine.shared.custom.contigents.api.CustomContingentsController;
import com.swisscom.travelmate.engine.shared.custom.contigents.service.CustomContingentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomContingentsControllerTests {

    @Mock
    private CustomContingentsService customContingentsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomContingentsController customContingentsController;

    private User user;
    private TravelRequest travelRequest;
    private UserGroup userGroup;
    private TravelRequestCompactedDto compactedDto;
    private LocalDate from = LocalDate.of(2024, 12, 1);
    private LocalDate until = LocalDate.of(2025, 12, 1);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // This is critical for injecting mocks

        user = User.createScaffold("123456");
        user.setId("userId1");

        userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("AO1")
                .userIds(new ArrayList<>(List.of("userId1")))
                .departmentNames(List.of("DVX", "BLG"))
                .build());

        travelRequest = TravelRequest.fromDto(TravelRequestDto.builder()
                .approvers(List.of("approver1"))
                .departureDate(LocalDate.of(2025, 5, 10))
                .returnDate(LocalDate.of(2025, 5, 20))
                .officeArrivalDate(LocalDate.of(2025, 5, 21))
                .travelDetails(List.of(TravelDetailsDto.builder().build()))
                .country(Country.IT)
                .metaData(Map.of("purpose", "Business Meeting"))
                .build(), user.getId());

        travelRequest.setStatus(TravelRequestStatus.CREATED);

        compactedDto = mock(TravelRequestCompactedDto.class);
    }

    @Test
    void testGetTravelRequestFromUserGroup_ReturnsOk_WhenUserGroupExists() {
        when(userService.getUserGroupById(userGroup.getId())).thenReturn(Optional.of(userGroup));
        when(customContingentsService.getApprovedTravelRequestsByUserGroup(userGroup, from, until))
                .thenReturn(List.of(compactedDto));

        ResponseEntity<List<TravelRequestCompactedDto>> response = customContingentsController.getTravelRequestFromUserGroup(userGroup.getId(), from, until);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of(compactedDto), response.getBody());
    }

    @Test
    void testGetTravelRequestFromUserGroup_ReturnsBadRequest_WhenUserGroupDoesNotExist() {
        when(userService.getUserGroupById(userGroup.getId())).thenReturn(Optional.empty());

        ResponseEntity<List<TravelRequestCompactedDto>> response = customContingentsController.getTravelRequestFromUserGroup(userGroup.getId(), from, until);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetTravelRequestFromUserGroup_ReturnsServerError_OnException() {
        when(userService.getUserGroupById(userGroup.getId())).thenThrow(new RuntimeException("Unexpected"));

        ResponseEntity<List<TravelRequestCompactedDto>> response = customContingentsController.getTravelRequestFromUserGroup(userGroup.getId(), from, until);

        assertEquals(500, response.getStatusCodeValue());
    }
}