package com.swisscom.travelmate.engine.ServiceTests.custom;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelDetailsDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import com.swisscom.travelmate.engine.modules.travelrequest.service.TravelRequestService;
import com.swisscom.travelmate.engine.modules.user.dto.UserGroupDto;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.custom.TravelRequestCompactedDto;
import com.swisscom.travelmate.engine.shared.custom.contigents.service.CustomContingentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.mockStatic;

class CustomContingentsServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private TravelRequestService travelRequestService;

    @InjectMocks
    private CustomContingentsService customContingentsService;

    private User user;
    private TravelRequest travelRequest;
    private UserGroup userGroup;
    private TravelRequestCompactedDto compactedDto;

    @BeforeEach
    void setUp() {
        openMocks(this);

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
    void testGetApprovedTravelRequestsByUserGroup() {
        when(travelRequestService.getApprovedTravelRequestsBetweenDatesByUserIds(
                anyList(), any(), any()))
                .thenReturn(List.of(travelRequest));

        when(userService.getUserDataRegardlessOfScaffolding(user.getId()))
                .thenReturn(Optional.of(user));

        try (var mocked = mockStatic(TravelRequestCompactedDto.class)) {
            mocked.when(() -> TravelRequestCompactedDto.fromTravelRequest(travelRequest, Optional.of(user)))
                    .thenReturn(compactedDto);

            List<TravelRequestCompactedDto> result = customContingentsService.getApprovedTravelRequestsByUserGroup(
                    userGroup, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

            assertEquals(1, result.size());
            assertSame(compactedDto, result.getFirst());
        }

        verify(travelRequestService).getApprovedTravelRequestsBetweenDatesByUserIds(anyList(), any(), any());
        verify(userService).getUserDataRegardlessOfScaffolding(user.getId());
    }

    @Test
    void testEmptyUserGroupReturnsEmptyList() {
        UserGroup emptyGroup = new UserGroup(null, null, null, null, null, null, null, null);
        emptyGroup.setUserIds(Collections.emptyList());

        List<TravelRequestCompactedDto> result = customContingentsService.getApprovedTravelRequestsByUserGroup(
                emptyGroup, LocalDate.now().minusDays(10), LocalDate.now());

        assertTrue(result.isEmpty());
        verifyNoInteractions(travelRequestService, userService);
    }

    @Test
    void testNoTravelRequestsFoundReturnsEmptyList() {
        when(travelRequestService.getApprovedTravelRequestsBetweenDatesByUserIds(
                anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<TravelRequestCompactedDto> result = customContingentsService.getApprovedTravelRequestsByUserGroup(
                userGroup, LocalDate.now().minusDays(10), LocalDate.now());

        assertTrue(result.isEmpty());
        verify(travelRequestService).getApprovedTravelRequestsBetweenDatesByUserIds(anyList(), any(), any());
        verifyNoInteractions(userService);
    }

    @Test
    void testUserNotFoundStillReturnsDto() {
        when(travelRequestService.getApprovedTravelRequestsBetweenDatesByUserIds(
                anyList(), any(), any()))
                .thenReturn(List.of(travelRequest));

        when(userService.getUserDataRegardlessOfScaffolding(user.getId()))
                .thenReturn(Optional.empty());

        try (var mocked = mockStatic(TravelRequestCompactedDto.class)) {
            mocked.when(() -> TravelRequestCompactedDto.fromTravelRequest(travelRequest, Optional.empty()))
                    .thenReturn(compactedDto);

            List<TravelRequestCompactedDto> result = customContingentsService.getApprovedTravelRequestsByUserGroup(
                    userGroup, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

            assertEquals(1, result.size());
            assertSame(compactedDto, result.getFirst());
        }

        verify(userService).getUserDataRegardlessOfScaffolding(user.getId());
    }
}