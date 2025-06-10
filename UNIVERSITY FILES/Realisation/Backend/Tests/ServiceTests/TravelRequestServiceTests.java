package com.swisscom.travelmate.engine.ServiceTests;

import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelDetailsDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestStatusDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelDetails;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import com.swisscom.travelmate.engine.modules.travelrequest.repository.TravelRequestRepository;
import com.swisscom.travelmate.engine.modules.travelrequest.service.TravelRequestService;
import com.swisscom.travelmate.engine.shared.enrichers.facades.PeopleGroupsAgileMembershipsEnrichmentFacade;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.custom.communitycalendar.service.CustomCommunityCalendarService;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.service.AnyOrgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource("/test.properties")
class TravelRequestServiceTests {

    @Mock
    private TravelRequestRepository travelRequestRepository;

    @Mock
    private AnyOrgService anyOrgService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TravelRequestService travelRequestService;

    @InjectMocks
    private CustomCommunityCalendarService customCommunityCalendarService;

    @Mock
    private PeopleGroupsAgileMembershipsEnrichmentFacade peopleGroupsAgileMembershipsEnrichmentFacade;

    private TravelRequest travelRequest;
    private List<TravelRequest> approvedTravelRequests;

    private User user;
    private PeopleGroupsAgileMembershipsDto peopleGroupsAgileMembershipsDto;

    @BeforeEach
    void setUp() {
        user = UserTestUtils.createUser("Darius", "van Essen");
        user.setEmployeeNr("12738912");

        travelRequest = TravelRequest.fromDto(TravelRequestDto.builder()
                .approvers(List.of("approver1"))
                .departureDate(LocalDate.of(2025, 5, 10))
                .returnDate(LocalDate.of(2025, 5, 20))
                .officeArrivalDate(LocalDate.of(2025, 5, 21))
                .travelDetails(List.of(TravelDetailsDto.builder().build())) // Assuming a default constructor exists
                .country(Country.IT)
                .metaData(Map.of("purpose", "Business Meeting"))
                .build(), user.getId());
        travelRequest.setStatus(TravelRequestStatus.CREATED);

        TravelRequest approvedTravelRequest1 = TravelRequest.fromDto(TravelRequestDto.builder()
            .approvers(List.of("approver2"))
            .departureDate(LocalDate.of(2025, 5, 10))
            .returnDate(LocalDate.of(2025, 5, 20))
            .officeArrivalDate(LocalDate.of(2025, 5, 21))
            .travelDetails(List.of(TravelDetailsDto.builder().build())) // Assuming a default constructor exists
            .country(Country.IT)
            .metaData(Map.of("purpose", "Business Meeting"))
            .build(), user.getId());
        approvedTravelRequest1.setStatus(TravelRequestStatus.APPROVED);
        approvedTravelRequest1.setTravelDetails(
            List.of(new TravelDetails(
                LocalDate.of(2025, 5, 10),
                true,
                "purpose",
                "category",
                "officeLocation 2, Office City",
                "Hotel place, Hotel"
            ))
        );
        approvedTravelRequest1.setUserId(user.getId());

        TravelRequest approvedTravelRequest2 = TravelRequest.fromDto(TravelRequestDto.builder()
            .approvers(List.of("approver2"))
            .departureDate(LocalDate.of(2025, 5, 10))
            .returnDate(LocalDate.of(2025, 5, 20))
            .officeArrivalDate(LocalDate.of(2025, 5, 21))
            .travelDetails(List.of(TravelDetailsDto.builder().build())) // Assuming a default constructor exists
            .country(Country.IT)
            .metaData(Map.of("purpose", "Business Meeting"))
            .build(), user.getId());
        approvedTravelRequest2.setStatus(TravelRequestStatus.APPROVED);
        approvedTravelRequest2.setTravelDetails(
            List.of(new TravelDetails(
                LocalDate.of(2025, 5, 10),
                true,
                "purpose",
                "category",
                "officeLocation 2, Office City",
                "Hotel place, Hotel"
            ))
        );
        approvedTravelRequest2.setUserId(user.getId());

        approvedTravelRequests = List.of(
            approvedTravelRequest1,
            approvedTravelRequest2
        );

        peopleGroupsAgileMembershipsDto = PeopleGroupsAgileMembershipsDto.builder()
                .id("RTE001")
                .employeeNr(1001)
                .roleId(200)
                .roleName("Agile Coach")
                .validFrom(new Date())
                .validTo(new Date(System.currentTimeMillis() + 86400000L)) // +1 day
                .internalUserId("approver1")
                .build();
    }

    @Test
    void testCheckUserCanChangeStatus_UnauthorizedUser() {
        travelRequest.setApprovers(List.of("approve1"));

        // Test when the user is neither an approver nor a deputy
        Boolean result = travelRequestService.checkUserCanChangeStatus(travelRequest, "unauthorizedUser");

        assertFalse(result, "Unauthorized user should not be able to change the status.");
    }

    @Test
    void testCheckUserCanChangeStatus_AuthorizedDeputy() {

        // Simulate the approvers list
        travelRequest.setApprovers(List.of("approve1"));

        // Test when the user is a deputy
        Boolean result = travelRequestService.checkUserCanChangeStatus(travelRequest, "approve1");

        assertTrue(result, "Deputy should be able to change the status.");
    }

    @Test
    void testUpdateTravelRequestStatus() {
        // Arrange: Mock repository save
        when(travelRequestRepository.save(travelRequest)).thenReturn(travelRequest);

        TravelRequestStatusDto travelRequestStatus = new TravelRequestStatusDto();
        travelRequestStatus.setStatus("APPROVED");

        // Act: Call the method to update the status
        TravelRequest updatedTravelRequest = travelRequestService.updateTravelRequestStatus(travelRequest, travelRequestStatus);

        // Assert: Verify the status update
        assertNotNull(updatedTravelRequest);
        assertEquals(TravelRequestStatus.APPROVED, updatedTravelRequest.getStatus(), "The status should be updated to APPROVED.");
        assertNotNull(updatedTravelRequest.getUpdatedAt(), "The updatedAt field should be set.");
        assertTrue(updatedTravelRequest.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)), "The updatedAt field should be close to the current time.");

        // Verify save was called on the repository
        verify(travelRequestRepository, times(1)).save(travelRequest);
    }

    @Test
    void testUpdateTravelRequestStatus_invalidStatus() {
        TravelRequestStatusDto travelRequestStatus = new TravelRequestStatusDto();
        travelRequestStatus.setStatus("INVALID_STATUS");

        // Act & Assert: Call the method and expect an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            travelRequestService.updateTravelRequestStatus(travelRequest, travelRequestStatus);
        }, "An IllegalArgumentException should be thrown for invalid status.");
    }

//    @Test
//    void testUpdateTravelRequest_validObject(){
//        TravelRequest updatedRequest = TravelRequestDto.builder()
//            .approvers(List.of("jan", "mitchel"))
//            .travelDetails(List.of(
//                new TravelDetailsDto()
//            ))
    @Test
    void testUpdateTravelRequest(){
        TravelRequestDto updateDto = TravelRequestDto.builder()
            .approvers(List.of("jan", "mitchel"))
            .travelDetails(List.of(
                TravelDetailsDto.builder().build()
            ))
            .departureDate(LocalDate.of(2025,1,1))
            .returnDate(LocalDate.of(2026,1,1))
            .country(Country.CH)
            .metaData(Map.of("cool", "cool"))
            .build();

        TravelRequest updateRequest = TravelRequest.fromDto(
            updateDto,
            user.getId()
        );
        TravelRequest expectedUpdateResult = TravelRequest.fromDto(
            updateDto,
            user.getId()
        );
        expectedUpdateResult.setId(travelRequest.getId());
        expectedUpdateResult.setStatus(travelRequest.getStatus());
        expectedUpdateResult.setCreatedAt(travelRequest.getCreatedAt());
        expectedUpdateResult.setUpdatedAt(travelRequest.getUpdatedAt());

        when(travelRequestRepository.save(any())).thenReturn(any());

        TravelRequest actualResult = travelRequestService.updateTravelRequest(travelRequest, updateRequest);
        expectedUpdateResult.setUpdatedAt(actualResult.getUpdatedAt());

        assertNotNull(actualResult.getUpdatedAt());
        assertEquals(actualResult.getId(), expectedUpdateResult.getId());
        assertEquals(actualResult.getApprovers(), expectedUpdateResult.getApprovers());
        assertEquals(actualResult.getDepartureDate(), expectedUpdateResult.getDepartureDate());
        assertEquals(actualResult.getReturnDate(), expectedUpdateResult.getReturnDate());

        assertNotEquals(actualResult.getApprovers(), travelRequest.getApprovers());
        assertNotEquals(actualResult.getDepartureDate(), travelRequest.getDepartureDate());
        assertNotEquals(actualResult.getReturnDate(), travelRequest.getReturnDate());

    }

    @Test
    void testCountTravelsThisYear() {
        String userId = "user123";
        int currentYear = LocalDate.now().getYear();
        Date lowerBoundDate = new Date(currentYear - 1 - 1900, Calendar.DECEMBER, 31); // December 31 of last year
        Date upperBoundDate = new Date(currentYear + 1 - 1900, Calendar.JANUARY, 1); // January 1 of the next year

        // Mock the repository call to return a specific count
        when(travelRequestRepository.countByUserIdAndDepartureDateBetween(userId, lowerBoundDate, upperBoundDate))
                .thenReturn(5); // Return a fixed number of travel requests

        // Act: Call the method to count travels for this year
        Integer travelCount = travelRequestService.countTravelsThisYear(userId);

        // Assert: Check that the correct count is returned
        assertEquals(5, travelCount, "The travel count should be 5 for the user this year.");

        // Verify the repository method was called with the correct arguments
        verify(travelRequestRepository, times(1))
                .countByUserIdAndDepartureDateBetween(eq(userId), eq(lowerBoundDate), eq(upperBoundDate));
    }

    @Test
    void testCountTravelsThisYear_NoTravels() {
        String userId = "user123";
        int currentYear = LocalDate.now().getYear();
        Date lowerBoundDate = new Date(currentYear - 1 - 1900, Calendar.DECEMBER, 31); // December 31 of last year
        Date upperBoundDate = new Date(currentYear + 1 - 1900, Calendar.JANUARY, 1); // January 1 of the next year

        // Mock the repository call to return 0 travels for this user
        when(travelRequestRepository.countByUserIdAndDepartureDateBetween(userId, lowerBoundDate, upperBoundDate))
                .thenReturn(0); // No travels for this user

        // Act: Call the method to count travels for this year
        Integer travelCount = travelRequestService.countTravelsThisYear(userId);

        // Assert: Check that the correct count is returned
        assertEquals(0, travelCount, "The travel count should be 0 for the user this year.");

        // Verify the repository method was called with the correct arguments
        verify(travelRequestRepository, times(1))
                .countByUserIdAndDepartureDateBetween(eq(userId), eq(lowerBoundDate), eq(upperBoundDate));
    }

    @Test
    void testCountTravelsThisYear_InvalidDateRange() {
        String userId = "user123";
        int currentYear = LocalDate.now().getYear();
        Date lowerBoundDate = new Date(currentYear - 1 - 1900, Calendar.DECEMBER, 31); // December 31 of last year
        Date upperBoundDate = new Date(currentYear + 1 - 1900, Calendar.JANUARY, 1); // January 1 of the next year

        // Mock the repository call to return a specific count
        when(travelRequestRepository.countByUserIdAndDepartureDateBetween(userId, lowerBoundDate, upperBoundDate))
                .thenReturn(3); // Return 3 travels for the user in the given range

        // Act: Call the method to count travels for this year
        Integer travelCount = travelRequestService.countTravelsThisYear(userId);

        // Assert: Check that the correct count is returned
        assertEquals(3, travelCount, "The travel count should be 3 for the user this year.");

        // Verify the repository method was called with the correct arguments
        verify(travelRequestRepository, times(1))
                .countByUserIdAndDepartureDateBetween(eq(userId), eq(lowerBoundDate), eq(upperBoundDate));
    }
}