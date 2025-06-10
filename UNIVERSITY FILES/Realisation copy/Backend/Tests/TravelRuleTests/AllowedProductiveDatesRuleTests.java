package com.swisscom.travelmate.engine.TravelRuleTests;

import com.swisscom.travelmate.engine.shared.util.DateUtils;
import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.rule.dto.TravelRuleSettingsDto;
import com.swisscom.travelmate.engine.modules.rule.evaluation.AllowedProductiveDatesRule;
import com.swisscom.travelmate.engine.modules.rule.evaluation.EvaluationResult;
import com.swisscom.travelmate.engine.modules.rule.model.TravelRuleSettings;
import com.swisscom.travelmate.engine.modules.rule.service.TravelRuleService;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelDetailsDto;
import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource("/test.properties")
public class AllowedProductiveDatesRuleTests {
    @InjectMocks
    private AllowedProductiveDatesRule rule;

    @Mock
    private TravelRuleService travelRuleService;

    private TravelRequest travelRequest;

    private TravelRuleSettings activeRule;


    private Calendar calendar = Calendar.getInstance();
    private User user;
    private List<Date> dateRange;

    @BeforeEach
    void setUp() {

        user = UserTestUtils.createUser("Darius", "van Essen");
        user.setId("user123");

        MockitoAnnotations.openMocks(this);

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

        travelRequest = TravelRequest.fromDto(TravelRequestDto.builder()
                .travelDetails(List.of(TravelDetailsDto.builder().build()))
                .approvers(List.of("approver1", "approver2"))
                .country(Country.IT)
                .metaData(Map.of("purpose", "Business Meeting", "budget", 5000))
                .build(), user.getId());

        dateRange = DateUtils.create10DayDateRange();
    }

    @Test
    void testAllowedDatesSuccess() {
        activeRule.setAllowedProductiveDates(dateRange);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(2)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(activeRule.getAllowedProductiveDates().get(3)));

        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertTrue(result.isSuccess());
    }

    @Test
    void testDisallowedDatesFailure() {
        activeRule.setAllowedProductiveDates(dateRange);

        // all dates are in allowed date range
        travelRequest.setDepartureDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 14)));
        travelRequest.setOfficeArrivalDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 15)));
        travelRequest.setReturnDate(DateUtils.convertToLocalDate(DateUtils.addDays(new Date(System.currentTimeMillis()), 16))); // outside of the allowed data range.
        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertFalse(result.isSuccess());
    }

    @Test
    void testEmptyAllowedDates() {
        activeRule.setAllowedProductiveDates(List.of());

        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertTrue(result.isSuccess());
    }

    @Test
    void testNullAllowedDates() {
        activeRule.setAllowedProductiveDates(null);

        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertTrue(result.isSuccess());
    }
}
