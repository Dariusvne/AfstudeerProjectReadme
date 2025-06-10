package com.swisscom.travelmate.engine.TravelRuleTests;

import com.swisscom.travelmate.engine.shared.util.DateUtils;
import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.rule.dto.TravelRuleSettingsDto;
import com.swisscom.travelmate.engine.modules.rule.evaluation.AllowedProductiveRule;
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
public class AllowedProductiveRuleTests {
    @InjectMocks
    private AllowedProductiveRule rule;

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
    }

    @Test
    void testAllowedProductiveSuccess() {
        activeRule.setAllowedProductive(true);

        travelRequest.getTravelDetails().get(0).setProductive(true);

        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertTrue(result.isSuccess());
    }

    @Test
    void testDisallowedProductiveFailure() {
        activeRule.setAllowedProductive(false);

        travelRequest.getTravelDetails().getFirst().setDate(DateUtils.convertToLocalDate(new Date(System.currentTimeMillis())));
        travelRequest.getTravelDetails().get(0).setProductive(true);
        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertFalse(result.isSuccess());
    }

    @Test
    void testNoProductiveDatesSuccess() {
        activeRule.setAllowedProductive(false);

        travelRequest.getTravelDetails().get(0).setProductive(false);

        EvaluationResult<Boolean> result = rule.process(travelRequest, List.of(activeRule), travelRuleService);

        assertTrue(result.isSuccess());
    }
}
