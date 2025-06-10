package com.swisscom.travelmate.engine.ServiceTests;

import com.swisscom.travelmate.engine.TestUtils.UserTestUtils;
import com.swisscom.travelmate.engine.modules.rule.dto.TravelRuleSettingsDto;
import com.swisscom.travelmate.engine.modules.rule.model.TravelRuleSettings;
import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import com.swisscom.travelmate.engine.modules.user.dto.UserDto;
import com.swisscom.travelmate.engine.modules.user.model.Deputy;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.repository.DeputyRepository;
import com.swisscom.travelmate.engine.modules.user.repository.UserGroupRepository;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.enrichers.facades.PeopleGroupsAgileMembershipsEnrichmentFacade;
import com.swisscom.travelmate.engine.shared.enrichers.facades.UserEnrichmentFacade;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.service.AnyOrgService;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import com.swisscom.travelmate.engine.shared.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnyOrgService anyOrgService;

    @Mock
    private WFIService wfiService;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private DeputyRepository deputyRepository;

    @Mock
    private UserEnrichmentFacade userEnrichmentFacade;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserById() {
        String userCode = "user123";
        String email = "user@example.com";
        Optional<User> expectedUser = Optional.of(UserTestUtils.createUserWithEmailAndCode(email, userCode));

        when(userRepository.findById(expectedUser.get().getId()))
                .thenReturn(expectedUser);

        Optional<User> actualUser = userService.getUser(expectedUser.get().getId());
        assertEquals(expectedUser, actualUser);
    }

    @Test
    public void testGetUserByUserCodeAndEmail() {
        String userCode = "user123";
        String email = "user@example.com";
        Optional<User> expectedUser = Optional.of(UserTestUtils.createUserWithEmailAndCode(email, userCode));

        when(userRepository.findByUserCodeAndEmail(userCode, email))
                .thenReturn(expectedUser);

        Optional<User> actualUser = userService.getUserByUserCodeAndEmail(userCode, email);
        assertEquals(expectedUser, actualUser);
    }

    @Test
    public void testGetUserByUserCodeAndEmail_wrongEmailandCode() {
        String userCode = "user123";
        String email = "user@example.com";
        Optional<User> expectedUser = Optional.of(UserTestUtils.createUserWithEmailAndCode(email, userCode));

        when(userRepository.findByUserCodeAndEmail(userCode, email))
                .thenReturn(expectedUser);

        Optional<User> actualUser = userService.getUserByUserCodeAndEmail("FAKE", "FAKE");
        assertFalse(actualUser.isPresent());
    }

    @Test
    public void testUpdateUser_existingUser() {
        // Setup
        UserDto updatedData = UserDto.builder()
                .firstNationality("Dutch")
                .homeCountry(Country.NL)
                .build();

        User existingUser = UserTestUtils.createUser("Darius", "van Essen");

        // Mock repository behavior
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> updatedUserOpt = userService.updateUser(existingUser.getId(), updatedData);

        // Assert presence
        assertTrue(updatedUserOpt.isPresent(), "User should be present after update");

        User updatedUser = updatedUserOpt.get();

        // Verify updated fields
        assertEquals("Darius", updatedUser.getFirstName());
        assertEquals("van Essen", updatedUser.getLastName());
        assertEquals("Dutch", updatedUser.getFirstNationality());
        assertEquals(Country.NL, updatedUser.getHomeCountry());
    }

    @Test
    public void testUpdateUser_NoExistingUser() {
        // Setup
        UserDto updatedData = UserDto.builder()
                .firstNationality("Dutch")
                .homeCountry(Country.NL)
                .build();

        User existingUser = UserTestUtils.createUser("Darius", "van Essen");

        // Mock repository behavior
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> updatedUserOpt = userService.updateUser("726782876278672", updatedData);

        // Assert presence
        assertFalse(updatedUserOpt.isPresent());
    }

    @Test
    public void testAddTravelRulesForUser() {
        // Arrange
        Date currentDate = new Date();
        TravelRuleSettingsDto travelRuleSettingsDto = TravelRuleSettingsDto.builder()
                .applicableFrom(currentDate)
                .applicableUntil(currentDate)
                .allowedOfficeLocationIds(List.of("NL"))
                .allowedProductiveDates(List.of(currentDate))
                .allowedUnproductiveDates(List.of(currentDate))
                .build();

        User existingUser = UserTestUtils.createUser("Darius", "van Essen");

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.addTravelRulesForUser(existingUser.getId(), travelRuleSettingsDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTravelRuleSettings().size());
        assertEquals(currentDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedProductiveDates()).getFirst());
        assertEquals(currentDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedUnproductiveDates()).getFirst());

        assertEquals("NL", Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getDestinationIds()).getFirst());
    }

    @Test
    public void testUpdateTravelRulesForUser() {
        // Arrange
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date();
        TravelRuleSettings travelRuleSettings = UserTestUtils.createTravelRuleSettingsWithApplicableDates(currentDate, currentDate);
        travelRuleSettings.setDestinationIds(List.of("NL"));
        travelRuleSettings.setAllowedProductiveDates(List.of(currentDate));
        travelRuleSettings.setAllowedUnproductiveDates(List.of(currentDate));


        User existingUser = UserTestUtils.createUser("Darius", "van Essen");
        existingUser.setTravelRuleSettings(List.of(travelRuleSettings));

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);


        calendar.add(Calendar.DAY_OF_MONTH, 4);
        Date newDate = calendar.getTime();
        TravelRuleSettingsDto updatedTravelSettings = TravelRuleSettingsDto.builder()
                .applicableFrom(currentDate)
                .applicableUntil(newDate)
                .allowedOfficeLocationIds(List.of("LV", "CH"))
                .allowedProductiveDates(List.of(newDate))
                .allowedUnproductiveDates(List.of(newDate))
                .build();
        // Act
        User result = userService.updateTravelRulesForUser(existingUser.getId(), travelRuleSettings.getId(), updatedTravelSettings);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTravelRuleSettings().size());
        assertNotEquals(currentDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedProductiveDates()).getFirst());
        assertNotEquals(currentDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedUnproductiveDates()).getFirst());
        assertNotEquals("NL", Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getDestinationIds()).getFirst());
        assertEquals("LV", Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getDestinationIds()).get(0));
        assertEquals("CH", Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getDestinationIds()).get(1));
        assertEquals(newDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedProductiveDates()).getFirst());
        assertEquals(newDate, Objects.requireNonNull(result.getTravelRuleSettings().getFirst().getAllowedUnproductiveDates()).getFirst());


    }

    @Test
    public void testDeleteTravelRulesForUser() {
        // Arrange
        Date currentDate = new Date();
        TravelRuleSettingsDto travelRuleSettingsDto = TravelRuleSettingsDto.builder()
                .applicableFrom(currentDate)
                .applicableUntil(currentDate)
                .allowedOfficeLocationIds(List.of("NL"))
                .allowedProductiveDates(List.of(currentDate))
                .allowedUnproductiveDates(List.of(currentDate))
                .build();

        TravelRuleSettings travelRuleSettings = TravelRuleSettings.fromDto(travelRuleSettingsDto);
        User existingUser = UserTestUtils.createUser("Darius", "van Essen");

        // mock
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.addTravelRulesForUser(existingUser.getId(), travelRuleSettings);

        // Check if added travel rules correctly
        assertEquals(existingUser.getTravelRuleSettings().getFirst(), travelRuleSettings);
        userService.removeTravelRulesForUser(existingUser.getId(), travelRuleSettings.getId());

        // Assert
        assertEquals(0, existingUser.getTravelRuleSettings().size());
    }

    @Test
    public void testCheckAndInitializeUser_existingUser() {
        // Arrange
        String userName = "TAAVADAD";
        String email = "Darius@swisscom.com";
        String userCode = "123";

        User existingUser = UserTestUtils.createUserWithEmailAndCode(email, userCode);

        // mock
        when(userRepository.findByUserCodeAndEmail(email, userCode)).thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.checkAndInitializeUser(email, userCode);

        // Assert
        assertEquals(result.getId(), existingUser.getId());
    }

    @Test
    public void testCheckAndInitializeUser_nonExistingUser() {
        // Arrange
        String email = "Darius@swisscom.com";
        String userCode = "TAAVADAD";
        String employeeNumber = "25675287";

        WFIUserDto wfiUserDto = WFIUserDto.builder().personalNumber(employeeNumber).orgOUName("DOS").firstName("Darius").lastName("van Essen").build();
        AgileMembershipsDto agileMembershipDto = AgileMembershipsDto.builder()
                .ntLogin(userCode)  // Replace with the actual user code if needed
                .firstName("Darius")
                .lastName("van Essen")
                .unitName("DOS")
                .employeeNr(employeeNumber)
                .build();

        List<AgileMembershipsDto> dtos = List.of(agileMembershipDto);
        ResponseEntity<List<AgileMembershipsDto>> agileMembershipsDtoResponse = ResponseEntity.ok(dtos);


        // mock
        when(wfiService.getUserByEmail(email)).thenReturn(Optional.of(wfiUserDto));
        when(anyOrgService.fetchAgileMembershipsWithSearch(SecurityUtils.conferFirstNameFromEmailAddress(email))).thenReturn(agileMembershipsDtoResponse);
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(anyString())).thenReturn(List.of());
        when(userRepository.findByUserCodeAndEmail(email, userCode)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);// return the inputted value
        when(userGroupRepository.findByAnyOrgObjectIdIn(List.of())).thenReturn(List.of());

        // Act
        User result = userService.checkAndInitializeUser(userCode, email);

        // Assert: check if the data from AnyOrg mock data is combined with the existing data in the user
        assertEquals("Darius", result.getFirstName());
        assertEquals("van Essen", result.getLastName());
        assertEquals("DOS", result.getDepartmentName());
        assertEquals(email, result.getEmail());
        assertEquals(userCode, result.getUserCode());
        assertEquals(employeeNumber, result.getEmployeeNr());
    }

    @Test
    public void testCheckAndInitializeUser_existingScaffoldedUser() {
        // Arrange
        String userName = "TAAVADAD";
        String email = "Darius@swisscom.com";
        String userCode = "TAAVADAD";
        String EmployeeNumber = "25675287";

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]); // return the inputted value
        User scaffoldedUser = userService.initializeScaffoldedUser(EmployeeNumber);
        WFIUserDto wfiUserDto = WFIUserDto.builder().personalNumber(EmployeeNumber).orgOUName("DOS").firstName("Darius").lastName("van Essen").build();

        AgileMembershipsDto agileMembershipDto = AgileMembershipsDto.builder()
                .ntLogin(userCode)  // Replace with the actual user code if needed
                .firstName("Darius")
                .lastName("van Essen")
                .unitName("DOS")
                .employeeNr("25675287")
                .build();

        List<AgileMembershipsDto> dtos = List.of(agileMembershipDto);
        ResponseEntity<List<AgileMembershipsDto>> e = new ResponseEntity<>(dtos, HttpStatusCode.valueOf(200));


        // mock
        when(wfiService.getUserByEmail(email)).thenReturn(Optional.of(wfiUserDto));
        when(anyOrgService.fetchAgileMembershipsWithSearch(SecurityUtils.conferFirstNameFromEmailAddress(email))).thenReturn(e);
        when(anyOrgService.fetchAllAgileObjectIdsForEmployeeNr(anyString())).thenReturn(List.of());
        when(userRepository.findByUserCodeAndEmail(email, userCode)).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNr(agileMembershipDto.employeeNr)).thenReturn(Optional.of(scaffoldedUser));
        when(userGroupRepository.findByAnyOrgObjectIdIn(List.of())).thenReturn(List.of());

        // Act
        User result = userService.checkAndInitializeUser(userCode, email);

        // Assert: check if the data from AnyOrg mock data is combined with the existing data in the user
        assertEquals("Darius", result.getFirstName());
        assertEquals("van Essen", result.getLastName());
        assertEquals("DOS", result.getDepartmentName());
        assertEquals(email, result.getEmail());
        assertEquals(userCode, result.getUserCode());
        assertEquals("25675287", result.getEmployeeNr());
    }

    @Test
    public void testUnScaffoldUserWithoutSaving(){
        String empNumber = "7149376";
        String userCode = "TAAVADAD";
        User user = User.createScaffold(empNumber);
        User unscaffoldedUser = new User(
                "Darius",
                "van Essen",
                "TAAVADAD",
                "darius.vanessen@swisscom.com",
                false,
                null,
                "83879828",
                "INI-DOS-DVX",
                null);

        WFIUserDto wfiUserDto = WFIUserDto.builder()
                .orgLineManager("manager-456")
                .orgOUName("IT Department")
                .personalNumber("PN7890")
                .preferredLanguage("en")
                .displayName("John Doe")
                .firstName("John")
                .lastName("Doe")
                .mailInternal("john.doe@swisscom.com")
                .orgLineManagerName("Jane Manager")
                .build();

        when(wfiService.getUserByEmployeeNumber(empNumber)).thenReturn(Optional.of(wfiUserDto));
        when(userEnrichmentFacade.scaffoldedUserEnricher(user)).thenReturn(unscaffoldedUser);

        User userResult = userService.unScaffoldUserWithoutSaving(user);

        assertEquals(unscaffoldedUser.getFirstName(), userResult.getFirstName());
        assertEquals(unscaffoldedUser.getLastName(), userResult.getLastName());
        assertEquals(unscaffoldedUser.getDepartmentName(), userResult.getDepartmentName());
        assertEquals(unscaffoldedUser.getEmail(), userResult.getEmail());
        assertEquals(unscaffoldedUser.getUserCode(), userResult.getUserCode());
        assertEquals(unscaffoldedUser.getEmployeeNr(), userResult.getEmployeeNr());
        assertEquals(unscaffoldedUser.getId(), userResult.getId());
    }

    @Test
    public void testUnScaffoldUserWithoutSaving_nonExistingEmployeeNumber(){
        String empNumber = "7149376";
        String userCode = "TAAVADAD";
        User user = User.createScaffold(empNumber);

        when(wfiService.getUserByEmployeeNumber(empNumber)).thenReturn(Optional.empty());
        when(userEnrichmentFacade.scaffoldedUserEnricher(user)).thenReturn(user);
        User userResult = userService.unScaffoldUserWithoutSaving(user);

        assertTrue(userResult.isScaffolded());
    }

    @Test
    public void testGetDeputies(){
        String userId = "123";

        when(deputyRepository.findByAdminUserId(userId)).thenReturn(
            List.of(
                new Deputy("deputyId1", userId, "456", new Date(), null),
                new Deputy("deputyId2", userId, "4567",new Date(), null)
            )
        );

        List<Deputy> deputies = userService.getDeputiesForUser(userId);
        assertEquals(deputies.size(), 2);
        assertEquals(deputies.stream().findFirst().get().getAdminUserId(), userId);
    }

}