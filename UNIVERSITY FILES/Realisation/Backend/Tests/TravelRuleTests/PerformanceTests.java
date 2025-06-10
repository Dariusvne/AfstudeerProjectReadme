package com.swisscom.travelmate.engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelDetails;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import com.swisscom.travelmate.engine.modules.travelrequest.repository.TravelRequestRepository;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.repository.UserGroupRepository;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.shared.custom.TravelRequestCompactedDto;
import com.swisscom.travelmate.engine.shared.custom.contigents.service.CustomContingentsService;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false", "spring.profiles.active=test"
})
@ActiveProfiles("test")
@TestPropertySource("/test.properties")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerformanceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private CustomContingentsService customContigentsService;

    @Value("${test.jwt.token}")
    private String jwt;

    private List<String> userIds;
    private UserGroup testGroup;
    private List<String> allUserIds;
    private List<String> userGroup100UserIds;
    private UserGroup largeTestGroup;

    @BeforeAll
    void setUpTestData() {
        userIds = new ArrayList<>();
        allUserIds = new ArrayList<>();
        userGroup100UserIds = new ArrayList<>();

        for (int i = 0; i < 250; i++) {
            String userId = "test-user-" + i;
            userIds.add(userId);

            User user = UserTestUtils.createUser("Test user " + i, "Lastname");
            user.setId(userId);
            user.setHomeCountry(Country.NL);
            user.setEmployeeNr("EMP-T" + i);
            user.setDepartmentName("INI-DOS-DVX");

            userRepository.save(user);

            TravelRequest request = new TravelRequest(null, null, null, null, null, null, null, null);
            request.setUserId(userId);
            request.setStatus(TravelRequestStatus.APPROVED);
            request.setDepartureDate(LocalDate.of(2025, 1, 1));
            request.setReturnDate(LocalDate.of(2025, 1, 10));
            request.setCountry(Country.CH);
            request.setTravelDetails(List.of(new TravelDetails(null, null, null, null, null, null)));
            travelRequestRepository.save(request);
        }

        testGroup = new UserGroup(null, null, null, null, null, null, null, null);
        testGroup.setId("test-group");
        testGroup.setUserIds(userIds);
        userGroupRepository.save(testGroup);

        // Insert 10,000 users, each with 2 travel requests
        for (int i = 0; i < 10000; i++) {
            String userId = "mass-user-" + i;
            allUserIds.add(userId);

            User user = UserTestUtils.createUser("User " + i, "LoadTest");
            user.setId(userId);
            user.setHomeCountry(Country.NL);
            user.setEmployeeNr("EMP" + i);
            user.setDepartmentName("INI-LOAD");
            userRepository.save(user);

            for (int j = 0; j < 2; j++) {
                TravelRequest request = new TravelRequest(null, null, null, null, null, null, null, null);
                request.setUserId(userId);
                request.setStatus(TravelRequestStatus.APPROVED);
                request.setDepartureDate(LocalDate.of(2025, 1, 1));
                request.setReturnDate(LocalDate.of(2025, 1, 5));
                request.setCountry(Country.CH);
                request.setTravelDetails(List.of(new TravelDetails(null, null, null, null, null, null)));
                travelRequestRepository.save(request);
            }

            // Add first 100 users to the user group
            if (i < 125) {
                userGroup100UserIds.add(userId);
            }
        }

        // Create a user group with the first 100 users
        largeTestGroup = new UserGroup(null, null, null, null, null, null, null, null);
        largeTestGroup.setId("large-test-group");
        largeTestGroup.setUserIds(userGroup100UserIds);
        userGroupRepository.save(largeTestGroup);
    }

    @Test
    void testTravelRequestEndpointPerformance_withRealToken() throws Exception {
        String url = String.format(
                "/v1/custom/contingents/travelrequests?userGroupId=%s&from=2025-01-01&until=2025-12-31",
                testGroup.getId()
        );

        long start = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwt)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        long duration = System.currentTimeMillis() - start;

        String jsonResponse = result.getResponse().getContentAsString();
        TravelRequestCompactedDto[] dtos = objectMapper.readValue(jsonResponse, TravelRequestCompactedDto[].class);

        assertEquals(250, dtos.length, "Expected 250 travel requests");
        assertTrue(duration <= 1500, "Request took too long: " + duration + "ms");
    }

    @Test
    void testTravelRequestEndpointPerformance_100x_withRealToken() throws Exception {
        String url = String.format(
                "/v1/custom/contingents/travelrequests?userGroupId=%s&from=2025-01-01&until=2025-12-31",
                testGroup.getId()
        );

        int totalRuns = 100;
        int failures = 0;
        long maxDuration = 0;

        for (int i = 0; i < totalRuns; i++) {
            long start = System.currentTimeMillis();

            MvcResult result = mockMvc.perform(get(url)
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            long duration = System.currentTimeMillis() - start;
            maxDuration = Math.max(maxDuration, duration);

            String jsonResponse = result.getResponse().getContentAsString();
            TravelRequestCompactedDto[] dtos = objectMapper.readValue(jsonResponse, TravelRequestCompactedDto[].class);

            if (dtos.length != 250 || duration > 1500) {
                failures++;

                System.out.printf("Run %d FAILED: length=%d, time=%dms%n", i + 1, dtos.length, duration);
            }
        }

        System.out.printf("Completed %d runs. Max duration: %d ms. Failures: %d.%n",
                totalRuns, maxDuration, failures);

        assertEquals(0, failures, "Some requests took too long or returned incorrect result count.");
    }

    @Test
    void testTravelRequestEndpointPerformance_massUserLoad() throws Exception {
        String url = String.format(
                "/v1/custom/contingents/travelrequests?userGroupId=%s&from=2025-01-01&until=2025-12-31",
                largeTestGroup.getId()
        );

        long start = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwt)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        long duration = System.currentTimeMillis() - start;

        String jsonResponse = result.getResponse().getContentAsString();
        TravelRequestCompactedDto[] dtos = objectMapper.readValue(jsonResponse, TravelRequestCompactedDto[].class);

        assertEquals(250, dtos.length, "Expected 200 travel requests (100 users × 2)");
        assertTrue(duration <= 1500, "Request took too long: " + duration + "ms");
    }

    @Test
    void testTravelRequestServiceCaching() {
        String groupId = testGroup.getId();
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate until = LocalDate.of(2025, 12, 31);

        Optional<UserGroup> optionalGroup = userGroupRepository.findById(groupId);
        assertTrue(optionalGroup.isPresent(), "Test user group should exist");

        UserGroup group = optionalGroup.get();

        // First call - populates the cache
        long t1 = System.nanoTime();
        List<TravelRequestCompactedDto> firstCall = customContigentsService.getApprovedTravelRequestsByUserGroup(group, from, until);
        long durationFirst = System.nanoTime() - t1;

        // Second call - should be served from cache
        long t2 = System.nanoTime();
        List<TravelRequestCompactedDto> secondCall = customContigentsService.getApprovedTravelRequestsByUserGroup(group, from, until);
        long durationSecond = System.nanoTime() - t2;

        System.out.printf("First call: %d ms, Cached call: %d ms%n", durationFirst / 1_000_000, durationSecond / 1_000_000);

        // Basic checks
        assertTrue(durationSecond < durationFirst, "Second call (cached) should be faster: " + durationSecond + " < " + durationFirst);
        assertTrue(durationSecond < 500_000_000, "Cached call should take <500ms"); // 100 million ns
    }

    @AfterAll
    void cleanupTestData() {
        // Clean 250-user test group
        travelRequestRepository.deleteAllByUserIdIn(userIds);
        userRepository.deleteAllById(userIds);
        userGroupRepository.deleteById(testGroup.getId());

        // Clean 10,000-user test group
        travelRequestRepository.deleteAllByUserIdIn(allUserIds);
        userRepository.deleteAllById(allUserIds);
        userGroupRepository.deleteById(largeTestGroup.getId());
    }
}