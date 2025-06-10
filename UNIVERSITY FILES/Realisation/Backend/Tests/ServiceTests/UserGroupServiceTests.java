package com.swisscom.travelmate.engine.ServiceTests;

import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.rule.dto.TravelRuleSettingsDto;
import com.swisscom.travelmate.engine.modules.rule.model.TravelRuleSettings;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.user.dto.UserGroupDto;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.repository.UserGroupRepository;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.service.AnyOrgService;
import com.swisscom.travelmate.engine.shared.util.SecurityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@TestPropertySource("/test.properties")
public class UserGroupServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private AnyOrgService anyOrgService;

    @InjectMocks
    private UserService userService;

    private List<UserGroup> agileObjectUserGroups;
    private UserGroup standardUserGroup;

    private TravelRuleSettings travelRuleSettings1;
    private TravelRuleSettings travelRuleSettings2;
    private static MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeAll
    static void init() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
    }

    @AfterAll
    static void close() {
        mockedSecurityUtils.close();
    }

    @BeforeEach
    public void setup() {
        mockedSecurityUtils.when(SecurityUtils::getInternalUserId).thenReturn("MockedUserId");
        MockitoAnnotations.initMocks(this);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);// return the inputted value

        // Create mock UserGroups using the provided syntax
        UserGroup agileGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("AO1")
                .userIds(new ArrayList<>(List.of("200", "201")))
                .departmentNames(List.of("DVX", "BLG"))
                .build());

        UserGroup agileGroup2 = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("AO2")
                .userIds(new ArrayList<>(List.of("202", "203")))
                .departmentNames(List.of("IIP", "MBM"))
                .build());

        agileObjectUserGroups = List.of(agileGroup, agileGroup2);

        // Create a mock UserGroup object
        standardUserGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("orgId1")  // Non-null AnyOrgObjectId
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))  // Example user IDs
                .build());


        travelRuleSettings1 = TravelRuleSettings.fromDto(TravelRuleSettingsDto.builder()
                .applicableFrom(new Date())
                .applicableUntil(new Date())
                .mandatoryReviewerIds(List.of("Reviewer1", "Reviewer2"))  // Optional field
                .optionalReviewerIds(List.of("OptionalReviewer1"))  // Optional field
                .allowedProductive(true)
                .maxTravelsPerYear(5)
                .build());

        travelRuleSettings2 = TravelRuleSettings.fromDto(TravelRuleSettingsDto.builder()
                .applicableFrom(new Date())
                .applicableUntil(new Date())
                .mandatoryReviewerIds(List.of("Reviewer3", "Reviewer4"))  // Optional field
                .optionalReviewerIds(List.of("OptionalReviewer2"))  // Optional field
                .allowedProductive(false)
                .maxTravelsPerYear(10)
                .build());
    }

    @Test
    public void testCreateUserGroup() {

        // Arrange
        UserGroupDto dto = UserGroupDto.builder()
                .name("Engineering Team")
                .description("Group for all software engineers")
                .userIds(List.of("101", "102", "103")).build();
        UserGroup mockUserGroup = UserGroup.fromDto(dto); // Mocked return object
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(mockUserGroup);

        // Act
        UserGroup result = userService.createUserGroup(dto);

        // Assert
        assertNotNull(result, "UserGroup should be created successfully");
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));

    }

    @Test
    public void testCreateUserGroupWithMultipleHardConfigs() {
        UserGroupDto dto = UserGroupDto.builder()
                .name("Invalid Group")
                .description("This should be rejected")
                .userIds(List.of("201", "202"))
                .departmentNames(List.of("Finance")) // Multiple hard configs - invalid
                .build();

        // Act
        UserGroup result = userService.createUserGroup(dto);

        // Assert
        assertNull(result, "Should return null due to multiple hard configurations");
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }

    @Test
    public void testCreateUserGroupWithDepartmentNames() {
        // Arrange
        UserGroupDto dto = UserGroupDto.builder()
                .name("HR Department")
                .description("HR Team members across locations")
                .departmentNames(List.of("HR", "Talent Acquisition")) // Only one hard config
                .build();

        UserGroup mockUserGroup = UserGroup.fromDto(dto);
        mockUserGroup.setDepartmentNames(dto.getDepartmentNames());

        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(mockUserGroup);

        // Mock UserRepository responses for department names
        User hrUsers = UserTestUtils.createUser("Wessel", "Wessel Meijer");
        User talentUsers = UserTestUtils.createUser("Darius", "van Essen");

        when(userRepository.findByDepartmentName("HR")).thenReturn(List.of(hrUsers));
        when(userRepository.findByDepartmentName("Talent Acquisition")).thenReturn(List.of(talentUsers));

        // Act
        UserGroup result = userService.createUserGroup(dto);

        // Assert
        assertNotNull(result);

        // Verify userRepository was called correctly
        verify(userRepository, times(1)).findByDepartmentName("HR");
        verify(userRepository, times(1)).findByDepartmentName("Talent Acquisition");

        // Verify that user IDs were updated correctly
        List<String> expectedUserIds = List.of(hrUsers.getId(), talentUsers.getId());
        assertEquals(expectedUserIds, result.getUserIds());
    }

    @Test
    public void testGetUserGroupsForUser_userInTwoGroups() {
        // Arrange
        String userId = "101";

        // Mock user groups returned from repository
        UserGroup group1 = UserGroup.fromDto(UserGroupDto.builder()
                .name("Engineering Team")
                .description("Group for all software engineers")
                .userIds(List.of("101", "102", "103")).build());

        UserGroup group2 = UserGroup.fromDto(UserGroupDto.builder()
                .name("Project X Team")
                .description("Cool group")
                .userIds(List.of("101", "202"))
                .build());

        List<UserGroup> mockUserGroups = List.of(group1, group2);
        when(userGroupRepository.findByUserIdsIn(List.of(userId))).thenReturn(mockUserGroups);

        // Act
        List<UserGroup> result = userService.getUserGroupsForUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Engineering Team", result.get(0).getName());
        assertEquals("Project X Team", result.get(1).getName());

        verify(userGroupRepository, times(1)).findByUserIdsIn(List.of(userId));
    }

    @Test
    public void testAddToRelevantUserGroups_countryAndDepartmentSpecified() {
        // Arrange
        User user = UserTestUtils.createUser("Wessel", "Wessel Meijer"); // Example user
        user.setEmployeeNr("EMP123");
        user.setHomeCountry(Country.NL);
        user.setDepartmentName("DVX");

        // Mock the external service call (fetchAllAgileObjectIdsForEmployeeNr)
        List<String> agileObjectIds = List.of("AO1", "AO2");
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(user.getEmployeeNr())).thenReturn(agileObjectIds);
        when(userGroupRepository.findByAnyOrgObjectIdIn(agileObjectIds)).thenReturn(agileObjectUserGroups);

        // Mock country-based user groups
        UserGroup countryGroup = UserGroup.fromDto(UserGroupDto.builder()
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))
                .build());
        List<UserGroup> countryUserGroups = List.of(countryGroup);
        when(userGroupRepository.findByCountry(Country.NL)).thenReturn(countryUserGroups);

        // Mock department-based user groups
        UserGroup departmentGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(List.of("DVX"))
                .userIds(new ArrayList<>(List.of("400", "401")))
                .build());
        List<UserGroup> departmentUserGroups = List.of(departmentGroup);
        when(userGroupRepository.findByDepartmentNamesContains("DVX")).thenReturn(departmentUserGroups);

        // Act
        List<UserGroup> result = userService.addToRelevantUserGroups(user);

        // Assert: Verify the updates to UserGroups
        assertNotNull(result);
        assertEquals(4, result.size(), "Should return 4 user groups.");

        // Verify that user IDs were updated correctly in each group
        assertTrue(agileObjectUserGroups.getFirst().getUserIds().contains(user.getId()));
        assertTrue(agileObjectUserGroups.get(1).getUserIds().contains(user.getId()));
        assertTrue(countryGroup.getUserIds().contains(user.getId()));
        assertTrue(departmentGroup.getUserIds().contains(user.getId()));
    }

    @Test
    public void testAddToRelevantUserGroups_countrySpecified() {
        // Arrange
        User user = UserTestUtils.createUser("Wessel", "Wessel Meijer"); // Example user
        user.setEmployeeNr("EMP123");
        user.setHomeCountry(Country.NL);

        // Mock the external service call (fetchAllAgileObjectIdsForEmployeeNr)
        List<String> agileObjectIds = List.of("AO1", "AO2");
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(user.getEmployeeNr())).thenReturn(agileObjectIds);
        when(userGroupRepository.findByAnyOrgObjectIdIn(agileObjectIds)).thenReturn(agileObjectUserGroups);

        // Mock country-based user groups
        UserGroup countryGroup = UserGroup.fromDto(UserGroupDto.builder()
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))
                .build());
        List<UserGroup> countryUserGroups = List.of(countryGroup);
        when(userGroupRepository.findByCountry(Country.NL)).thenReturn(countryUserGroups);

        // Mock department-based user groups
        UserGroup departmentGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(List.of("DVX"))
                .userIds(new ArrayList<>(List.of("400", "401")))
                .build());
        List<UserGroup> departmentUserGroups = List.of(departmentGroup);
        when(userGroupRepository.findByDepartmentNamesContains("DVX")).thenReturn(departmentUserGroups);

        // Act
        List<UserGroup> result = userService.addToRelevantUserGroups(user);

        // Assert: Verify the updates to UserGroups
        assertNotNull(result);
        assertEquals(3, result.size(), "Should return 4 user groups.");

        // Verify that user IDs were updated correctly in each group
        assertTrue(agileObjectUserGroups.getFirst().getUserIds().contains(user.getId()));
        assertTrue(agileObjectUserGroups.get(1).getUserIds().contains(user.getId()));
        assertTrue(countryGroup.getUserIds().contains(user.getId()));
        assertFalse(departmentGroup.getUserIds().contains(user.getId()));
    }

    @Test
    public void testAddToRelevantUserGroups_departmentSpecified() {
        // Arrange
        User user = UserTestUtils.createUser("Wessel", "Wessel Meijer"); // Example user
        user.setEmployeeNr("EMP123");
        user.setDepartmentName("IIP");

        // Mock the external service call (fetchAllAgileObjectIdsForEmployeeNr)
        List<String> agileObjectIds = List.of("AO1", "AO2");
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(user.getEmployeeNr())).thenReturn(agileObjectIds);
        when(userGroupRepository.findByAnyOrgObjectIdIn(agileObjectIds)).thenReturn(agileObjectUserGroups);

        // Mock country-based user groups
        UserGroup countryGroup = UserGroup.fromDto(UserGroupDto.builder()
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))
                .build());
        List<UserGroup> countryUserGroups = List.of(countryGroup);
        when(userGroupRepository.findByCountry(Country.NL)).thenReturn(countryUserGroups);

        // Mock department-based user groups
        UserGroup departmentGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(List.of("IIP"))
                .userIds(new ArrayList<>(List.of("400", "401")))
                .build());
        List<UserGroup> departmentUserGroups = List.of(departmentGroup);
        when(userGroupRepository.findByDepartmentNamesContains("IIP")).thenReturn(departmentUserGroups);

        // Act
        List<UserGroup> result = userService.addToRelevantUserGroups(user);

        // Assert: Verify the updates to UserGroups
        assertNotNull(result);
        assertEquals(3, result.size(), "Should return 4 user groups.");

        // Verify that user IDs were updated correctly in each group
        assertTrue(agileObjectUserGroups.getFirst().getUserIds().contains(user.getId()));
        assertTrue(agileObjectUserGroups.get(1).getUserIds().contains(user.getId()));
        assertFalse(countryGroup.getUserIds().contains(user.getId()));
        assertTrue(departmentGroup.getUserIds().contains(user.getId()));
    }

    @Test
    public void testAddToRelevantUserGroups_onlyAgileGroups() {
        // Arrange
        User user = UserTestUtils.createUser("Wessel", "Wessel Meijer"); // Example user
        user.setEmployeeNr("EMP123");

        // Mock the external service call (fetchAllAgileObjectIdsForEmployeeNr)
        List<String> agileObjectIds = List.of("AO1", "AO2");
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(user.getEmployeeNr())).thenReturn(agileObjectIds);
        when(userGroupRepository.findByAnyOrgObjectIdIn(agileObjectIds)).thenReturn(agileObjectUserGroups);

        // Mock country-based user groups
        UserGroup countryGroup = UserGroup.fromDto(UserGroupDto.builder()
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))
                .build());
        List<UserGroup> countryUserGroups = List.of(countryGroup);
        when(userGroupRepository.findByCountry(Country.NL)).thenReturn(countryUserGroups);

        // Mock department-based user groups
        UserGroup departmentGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(List.of("IIP"))
                .userIds(new ArrayList<>(List.of("400", "401")))
                .build());
        List<UserGroup> departmentUserGroups = List.of(departmentGroup);
        when(userGroupRepository.findByDepartmentNamesContains("IIP")).thenReturn(departmentUserGroups);

        // Act
        List<UserGroup> result = userService.addToRelevantUserGroups(user);

        // Assert: Verify the updates to UserGroups
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 4 user groups.");

        // Verify that user IDs were updated correctly in each group
        assertTrue(agileObjectUserGroups.getFirst().getUserIds().contains(user.getId()));
        assertTrue(agileObjectUserGroups.get(1).getUserIds().contains(user.getId()));
        assertFalse(countryGroup.getUserIds().contains(user.getId()));
        assertFalse(departmentGroup.getUserIds().contains(user.getId()));
    }

    @Test
    public void testRefreshAnyOrgUserGroup() {
        User user = UserTestUtils.createUser("Darius", "van Essen"); // Example user
        user.setEmployeeNr("2879827892");

        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .country(Country.NL)  // Set country as Netherlands
                .userIds(new ArrayList<>(List.of("300", user.getId())))  // Example user IDs
                .build());

        // Mocks for the service call
        List<AgileMembershipsDto> agileMembers = new ArrayList<>();
        agileMembers.add(AgileMembershipsDto.builder()
                .employeeNr("28787278")
                .ntLogin("TAAVADAD")
                .firstName("Darius")
                .lastName("van Essen")
                .isValid(true)
                .build());

        agileMembers.add(AgileMembershipsDto.builder()
                .employeeNr("emp2")
                .ntLogin("asmith")
                .firstName("Alice")
                .lastName("Smith")
                .isValid(true)
                .build());

        when(anyOrgService.getAgileObjectsChildrenRecursively(anyString())).thenReturn(Arrays.asList("agileObj1", "agileObj2"));
        when(anyOrgService.fetchAgileObjectsAgileMembershipsByIds(anyList())).thenReturn(agileMembers);

        // Mocks for the repository call
        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(user);

        when(userRepository.findByEmployeeNrIn(anyList())).thenReturn(existingUsers);


        // Call the method under test
        UserGroup updatedUserGroup = userService.refreshAnyOrgUserGroup(userGroup);

        // Assertions
        assertNotNull(updatedUserGroup);
        assertEquals(3, updatedUserGroup.getUserIds().size());
        verify(userGroupRepository).save(updatedUserGroup);
    }

    @Test
    public void testRefreshDepartmentNamesUserGroup_withUsersInDepartments() {

        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(new ArrayList<>(List.of("IIP", "DVX")))  // Example departments
                .build());
        // Mock data: Users in the "HR" and "IT" departments
        User user = UserTestUtils.createUser("Darius", "van Essen"); // Example user
        user.setDepartmentName("IIP");
        User user2 = UserTestUtils.createUser("Wessel", "Meijer"); // Example user
        user.setDepartmentName("DVX");

        // Mock the userRepository calls to return users for the given departments
        when(userRepository.findByDepartmentName("IIP")).thenReturn(List.of(user));
        when(userRepository.findByDepartmentName("DVX")).thenReturn(List.of(user2));

        assertEquals(0, userGroup.getUserIds().size());
        // Call the method under test
        UserGroup updatedUserGroup = userService.refreshDepartmentNamesUserGroup(userGroup);

        // Assertions
        assertNotNull(updatedUserGroup);
        assertEquals(2, updatedUserGroup.getUserIds().size()); // Should include HR and IT users
        assertTrue(updatedUserGroup.getUserIds().contains(user.getId())); // Check if HR user is added
        assertTrue(updatedUserGroup.getUserIds().contains(user.getId())); // Check if IT user is added
        verify(userGroupRepository).save(updatedUserGroup);  // Verify that the user group was saved
    }

    @Test
    public void testRefreshDepartmentNamesUserGroup_withNoUsersInDepartments() {
        // Create a UserGroup object with departments
        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(new ArrayList<>(List.of("IIP", "DVX")))  // Example departments
                .build());

        // Mock the userRepository to return empty lists for both departments
        when(userRepository.findByDepartmentName("IIP")).thenReturn(Collections.emptyList());
        when(userRepository.findByDepartmentName("DVX")).thenReturn(Collections.emptyList());

        // Initial userIds should be empty
        assertEquals(0, userGroup.getUserIds().size());

        // Call the method under test
        UserGroup updatedUserGroup = userService.refreshDepartmentNamesUserGroup(userGroup);

        // Assertions
        assertNotNull(updatedUserGroup);
        assertTrue(updatedUserGroup.getUserIds().isEmpty());  // No users should be added
        verify(userGroupRepository).save(updatedUserGroup);  // Verify that the user group was saved

    }

    @Test
    public void testRefreshDepartmentNamesUserGroup_withDuplicateUsersAcrossDepartments() {
        // Create a UserGroup object with departments
        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .departmentNames(new ArrayList<>(List.of("IIP", "DVX")))  // Example departments
                .build());

        // Mock data: Users in both the "IIP" and "DVX" departments
        User user = UserTestUtils.createUser("Darius", "van Essen"); // Example user
        user.setDepartmentName("IIP");
        User user2 = UserTestUtils.createUser("Wessel", "Meijer"); // Example user
        user2.setDepartmentName("DVX");

        // Mock the userRepository calls to return duplicate users across departments
        when(userRepository.findByDepartmentName("IIP")).thenReturn(List.of(user));
        when(userRepository.findByDepartmentName("DVX")).thenReturn(List.of(user2));

        // Call the method under test
        UserGroup updatedUserGroup = userService.refreshDepartmentNamesUserGroup(userGroup);

        // Assertions
        assertNotNull(updatedUserGroup);
        assertEquals(2, updatedUserGroup.getUserIds().size());  // Should include both users (unique)
        assertTrue(updatedUserGroup.getUserIds().contains(user.getId()));  // Check if user is added
        assertTrue(updatedUserGroup.getUserIds().contains(user2.getId()));  // Check if user2 is added
        verify(userGroupRepository).save(updatedUserGroup);  // Verify that the user group was saved
    }


    @Test
    public void testAddTravelRulesForUserGroup() {
        // Arrange
        String userGroupId = "12345";

        // Mock the repository calls
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(standardUserGroup));

        // Act
        UserGroup result = userService.addTravelRulesForUserGroup(userGroupId, travelRuleSettings1);

        // Assert: Verify that the travel rule is added to the user group
        assertNotNull(result);
        assertEquals(1, result.getTravelRuleSettings().size(), "Should have 1 travel rule added.");
        assertTrue(result.getTravelRuleSettings().contains(travelRuleSettings1), "Travel rule should be present in the list.");
        verify(userGroupRepository).save(result);  // Verify that the user group was saved
    }

    @Test
    public void testAddTravelRulesForUserGroup_nonExistingUserGroup() {
        // Arrange
        String userGroupId = "12345";

        // Mock the repository calls
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.empty());

        // Act
        UserGroup result = userService.addTravelRulesForUserGroup(userGroupId, travelRuleSettings1);
        assertNull(result);
    }

    @Test
    public void testUpdateTravelRulesForUserGroup() {
        // Arrange
        String userGroupId = "12345";
        String travelRulesId = "travelRule1";
        travelRuleSettings1.setId(travelRulesId);

        standardUserGroup.setId(userGroupId);
        standardUserGroup.setTravelRuleSettings(List.of(travelRuleSettings1));

        // Mock the repository call to return the user group
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(standardUserGroup));
        TravelRuleSettingsDto updatedTravelRuleSetting = TravelRuleSettingsDto.builder()
                .applicableFrom(new Date())
                .applicableUntil(new Date())
                .mandatoryReviewerIds(List.of("Reviewer3", "Reviewer4"))  // Optional field
                .optionalReviewerIds(List.of("OptionalReviewer2"))  // Optional field
                .allowedProductive(false)
                .maxTravelsPerYear(10)
                .build();

        // Act
        UserGroup result = userService.updateTravelRulesForUserGroup(userGroupId, travelRulesId, updatedTravelRuleSetting);

        // Assert: Verify that the travel rule is updated
        assertNotNull(result, "User group should not be null.");
        assertEquals(1, result.getTravelRuleSettings().size(), "User group should have 1 travel rule.");
        TravelRuleSettings updatedRule = result.getTravelRuleSettings().get(0);
        assertEquals(travelRulesId, updatedRule.getId(), "The travel rule ID should match.");
        assertEquals(10, updatedRule.getMaxTravelsPerYear(), "The 'maxTravelsPerYear' should be updated.");
        verify(userGroupRepository).save(result);  // Verify that the user group was saved
    }

    @Test
    public void testUpdateTravelRulesForUserGroup_WhenTravelRuleNotFound() {
        // Arrange
        String userGroupId = "12345";
        String travelRulesId = "nonExistentTravelRule";

        standardUserGroup.setId(userGroupId);
        standardUserGroup.setTravelRuleSettings(List.of());

        // Mock the repository call to return the user group
        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(standardUserGroup));

        // Create empty travelrulesetting
        TravelRuleSettingsDto updatedTravelRuleSettingsDto = TravelRuleSettingsDto.builder()
                .applicableFrom(new Date())
                .applicableUntil(new Date())
                .build();

        // Act
        UserGroup result = userService.updateTravelRulesForUserGroup(userGroupId, travelRulesId, updatedTravelRuleSettingsDto);

        // Assert: Verify that the method returns null when the travel rule is not found
        assertNull(result, "User group should be null when the travel rule is not found.");
    }

    @Test
    public void testRemoveTravelRulesForUserGroup() {
        // Arrange
        String userGroupId = "userGroup1";

        // Create a UserGroup with the original travel rules
        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("orgId1")  // Non-null AnyOrgObjectId
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))  // Example user IDs
                .build());
        userGroup.setId(userGroupId);
        userGroup.setTravelRuleSettings(List.of(travelRuleSettings1, travelRuleSettings2));

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));

        // Act
        UserGroup updatedUserGroup = userService.removeTravelRulesForUserGroup(userGroupId, travelRuleSettings2.getId());

        // Assert
        assertNotNull(updatedUserGroup);
        assertEquals(1, updatedUserGroup.getTravelRuleSettings().size(), "Should have one travel rule left.");
        assertFalse(updatedUserGroup.getTravelRuleSettings().stream().anyMatch(rule -> rule.getId().equals(travelRuleSettings2.getId())),
                "Removed travel rule should not exist in the list.");

        // Verify that the repository save was called
        verify(userGroupRepository).save(updatedUserGroup);
    }

    @Test
    public void testRemoveTravelRulesForUserGroup_NoRuleRemoved() {
        // Arrange
        String userGroupId = "userGroup1";
        String travelRulesIdToRemove = "nonExistentTravelRule";  // ID that doesn't exist in the group

        UserGroup userGroup = UserGroup.fromDto(UserGroupDto.builder()
                .anyOrgObjectId("orgId1")  // Non-null AnyOrgObjectId
                .country(Country.NL)
                .userIds(new ArrayList<>(List.of("300", "301")))  // Example user IDs
                .build());
        userGroup.setId(userGroupId);
        userGroup.setTravelRuleSettings(List.of(travelRuleSettings1));

        when(userGroupRepository.findById(userGroupId)).thenReturn(Optional.of(userGroup));

        // Act
        UserGroup updatedUserGroup = userService.removeTravelRulesForUserGroup(userGroupId, travelRulesIdToRemove);

        // Assert
        assertNull(updatedUserGroup, "Should return null since no travel rule was removed.");
        verify(userGroupRepository, never()).save(any(UserGroup.class));  // Verify that save was never called
    }
}