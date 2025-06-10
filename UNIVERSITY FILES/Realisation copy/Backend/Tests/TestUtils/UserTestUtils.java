package com.swisscom.travelmate.engine.TestUtils;

import com.swisscom.travelmate.engine.modules.rule.model.TravelRuleSettings;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserTestUtils {
    public static User createUser(String name, String lastName) {
        return new User(name, lastName, null, null, false, null, null, null, null);
    }

    public static User createUserWithEmailAndCode(String email, String userCode) {
        return new User(null, null, userCode, email, false, null, null, null, null);
    }

    public static User createUserWithEmailAndEmployeeNr(String email, String employeeNr) {
        return new User(null, null, null, email, false, null, employeeNr, null, null);
    }

    public static TravelRuleSettings createTravelRuleSettingsWithApplicableDates(Date applicableFrom, Date applicableUntil){
        return new TravelRuleSettings(UUID.randomUUID().toString(), applicableFrom, applicableUntil, null, null, null, null,null, null, null, null, null, null);
    }

    public static UserGroup createEmptyUserGroup(){
        return new UserGroup(UUID.randomUUID().toString(), null, null, null, null, null, null, null);
    }
}