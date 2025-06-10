package com.swisscom.travelmate.engine.ServiceTests;

import com.swisscom.travelmate.engine.shared.util.DateUtils;
import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.rule.dto.TravelRuleSettingsDto;
import com.swisscom.travelmate.engine.modules.rule.model.TravelRuleSettings;
import com.swisscom.travelmate.engine.modules.rule.service.TravelRuleService;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelDetailsDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.service.TravelRequestService;
import com.swisscom.travelmate.engine.modules.user.dto.UserGroupDto;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class TravelRuleServiceTests {

    @InjectMocks
    private TravelRuleService travelRuleService;

    @Mock
    private TravelRequestService travelRequestService;

    @Mock
    private UserService userService;

    private User user;
    private UserGroup defaultUserGroup;
    private TravelRuleSettings activeRule;
    private TravelRuleSettings inactiveRule;

    private TravelRequest travelRequest;

    private Calendar calendar = Calendar.getInstance();
    private List<Date> dateRange;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        user = UserTestUtils.createUser("Darius", "van Essen");
        user.setId("user123");

        dateRange = DateUtils.create10DayDateRange();

        // Active Travel Rule
        activeRule = TravelRuleSettings.fromDto(TravelRuleSettingsDto.builder()
                .allowedOfficeLocationIds(List.of("NL"))
                .build());

        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, -10);
        activeRule.setApplicableFrom(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, +20);
        activeRule.setApplicableUntil(calendar.getTime());
        calendar.clear();

        // Inactive Travel Rule
        inactiveRule = TravelRuleSettings.fromDto(TravelRuleSettingsDto.builder()
                .applicableFrom(DateUtils.addDays(new Date(System.currentTimeMillis()), 10)) // 10 days in the past
                .applicableUntil(DateUtils.removeDays(new Date(System.currentTimeMillis()), 5)) // 5 days in the past
                .allowedOfficeLocationIds(List.of("CH"))
                .build());

        defaultUserGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("orgId1")  // Non-null AnyOrgObjectId
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))  // Example user IDs
                .build());

        travelRequest = TravelRequest.fromDto(TravelRequestDto.builder()
                .travelDetails(List.of(TravelDetailsDto.builder().build()))
                .approvers(List.of("approver1", "approver2"))
                .country(Country.IT)
                .metaData(Map.of("purpose", "Business Meeting", "budget", 5000))
                .build(), user.getId());

        user.setTravelRuleSettings(Arrays.asList(activeRule, inactiveRule));
        defaultUserGroup.setTravelRuleSettings(Arrays.asList(activeRule, inactiveRule));
    }

    @Test
    void testGetActiveTravelRuleSettingsForUserAndTravelRequest() {
        Date testDate = new Date();

        when(userService.getUser("user123")).thenReturn(Optional.of(user));
        when(userService.getUserGroupsForUser("user123")).thenReturn(List.of(defaultUserGroup));

        List<TravelRuleSettings> activeRules = travelRuleService.getApplicableTravelRuleSettingsForUserAndTravelRequest("user123", testDate, travelRequest);

        // Only 2 active rules should be returned (1 from user + 1 from group)
        assertEquals(2, activeRules.size());
        assertTrue(activeRules.contains(activeRule));
        assertFalse(activeRules.contains(inactiveRule));

        verify(userService, times(1)).getUser("user123");
        verify(userService, times(1)).getUserGroupsForUser("user123");
    }

    @Test
    void testGetActiveTravelRuleSettingsForUserWithNoRules() {
        Date testDate = new Date();
        user.setTravelRuleSettings(List.of()); // No travel rules

        when(userService.getUser("user123")).thenReturn(Optional.of(user));
        when(userService.getUserGroupsForUser("user123")).thenReturn(List.of());

        List<TravelRuleSettings> activeRules = travelRuleService.getApplicableTravelRuleSettingsForUserAndTravelRequest("user123", testDate, travelRequest);

        assertTrue(activeRules.isEmpty(), "Expected no active travel rules since the user has none.");
    }

    @Test
    void testEvaluateIncomingTravelRequest_NoActiveRules() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.isEmpty(), "Expected no failure messages when there are no active rules.");
    }

    @Test
    void testEvaluateIncomingTravelRequest_MaxTravelsPerYearRuleFails() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));
        activeRule.setMaxTravelsPerYear(4);

        when(travelRequestService.countTravelsThisYear(user.getId())).thenReturn(10);
        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.getFirst().containsKey("MaxTravelsRule"));
        assertTrue(result.getFirst().containsValue("Already have 10/4 travels done"));
    }

    @Test
    void testEvaluateIncomingTravelRequest_MaxTravelsPerYearRuleSuccess() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));
        activeRule.setMaxTravelsPerYear(4);

        when(travelRequestService.countTravelsThisYear(user.getId())).thenReturn(2);
        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedProductiveDatesSucceeds() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedProductiveDates(dateRange);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedProductiveDatesFails() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedProductiveDates(dateRange);

        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 15))); // outside of the allowed data range.

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.getFirst().containsKey("AllowedProductiveDatesRule"));
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedUnProductiveDatesSucceeds() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedUnproductiveDates(dateRange);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedUnProductiveDatesFails() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedUnproductiveDates(dateRange);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 15))); // outside of the allowed data range.

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.getFirst().containsKey("AllowedUnproductiveDatesRule"));
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedProductiveSucceeds() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedProductiveDates(dateRange);
        activeRule.setAllowedUnproductiveDates(dateRange);
        activeRule.setAllowedProductive(true);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));

        travelRequest.getTravelDetails().getFirst().setProductive(false);
        travelRequest.getTravelDetails().getFirst().setDate(DateUtils.convertToLocalDate(new Date(System.currentTimeMillis())));

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testEvaluateIncomingTravelRequest_allowedProductiveFails() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedProductiveDates(dateRange);
        activeRule.setAllowedUnproductiveDates(dateRange);
        activeRule.setAllowedProductive(false);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));

        travelRequest.getTravelDetails().getFirst().setProductive(true);
        travelRequest.getTravelDetails().getFirst().setDate(DateUtils.convertToLocalDate(new Date(System.currentTimeMillis())));


        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().containsKey("AllowedProductiveRule"));
    }

    @Test
    void testEvaluateIncomingTravelRequest_multipleRulesFails() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedUnproductiveDates(dateRange);
        activeRule.setAllowedProductiveDates(dateRange);
        activeRule.setMaxTravelsPerYear(3);

        // More than allowed travel days per year.
        when(travelRequestService.countTravelsThisYear(user.getId())).thenReturn(10);

        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate((activeRule.getAllowedUnproductiveDates().get(3))));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 15))); // outside of the allowed data range.

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertEquals(3, result.size());
        assertTrue(result.get(0).containsKey("AllowedUnproductiveDatesRule"));
        assertTrue(result.get(1).containsKey("AllowedProductiveDatesRule"));
        assertTrue(result.get(2).containsKey("MaxTravelsRule"));

    }

    @Test
    void testEvaluateIncomingTravelRequest_oneSucceededRuleAndOneFailedRule() {
        when(userService.getUser("user123")).thenReturn(Optional.of(user));

        activeRule.setAllowedUnproductiveDates(dateRange);
        activeRule.setAllowedProductiveDates(dateRange);
        activeRule.setMaxTravelsPerYear(3);

        // More than allowed travel days per year.
        when(travelRequestService.countTravelsThisYear(user.getId())).thenReturn(10);

        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate((activeRule.getAllowedUnproductiveDates().get(3))));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedUnproductiveDates().get(3)));

        List<Map<String, String>> result = travelRuleService.evaluateIncomingTravelRequest("user123", travelRequest);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().containsKey("MaxTravelsRule"));

    }

}