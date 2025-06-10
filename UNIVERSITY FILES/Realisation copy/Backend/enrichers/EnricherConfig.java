package com.swisscom.travelmate.engine.shared.enrichers;

import com.swisscom.travelmate.engine.shared.enrichers.custom.email.ScaffoldedUserEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.name.PeopleGroupsAgileMembershipsNameEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.email.AgileMembershipsEmailEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.email.PeopleGroupsAgileMembershipsEmailEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.email.UserEmailEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.userid.AgileMembershipsUserIdEnricher;
import com.swisscom.travelmate.engine.shared.enrichers.custom.userid.PeopleGroupsAgileMembershipsUserIdEnricher;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.external.anyorg.client.AnyOrgClient;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M.SecureM2MJwtService;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class EnricherConfig {

    @Value("${anyorg.client.id}")
    private String anyOrgClientId;

    @Value("${anyorg.client.secret}")
    private String anyOrgClientSecret;

    // User Enrichers
    @Bean
    public Enricher<User> userEmailEnricher(WFIService wfiService) {
        return new UserEmailEnricher(wfiService);
    }

    @Bean
    public Enricher<User> scaffoldedUserEnricher(WFIService wfiService) { return new ScaffoldedUserEnricher(wfiService); }

    // AgileMemberships Enrichers
    @Bean
    public Enricher<List<AgileMembershipsDto>> agileMembershipsFullEnricher(WFIService wfiService, UserRepository userRepository, UserService userService) {
        return new CompositeEnricher<>(
                new AgileMembershipsUserIdEnricher(userRepository, userService),
                new AgileMembershipsEmailEnricher(wfiService)
        );
    }


    //PeopleGroupsAgileMemberships Enrichers
    @Bean
    public Enricher<List<PeopleGroupsAgileMembershipsDto>> peopleGroupsAgileMembershipsUserIdAndEmailEnricher(WFIService wfiService, UserRepository userRepository, UserService userService) {
        return new CompositeEnricher<>(
                new PeopleGroupsAgileMembershipsUserIdEnricher(userRepository, userService),
                new PeopleGroupsAgileMembershipsEmailEnricher(wfiService)
                );
    }

    @Bean
    public Enricher<List<PeopleGroupsAgileMembershipsDto>> peopleGroupsAgileMembershipsUserIdEnricher(UserRepository userRepository, UserService userService) {
        return new PeopleGroupsAgileMembershipsUserIdEnricher(userRepository, userService);
    }

    @Bean
    public Enricher<List<PeopleGroupsAgileMembershipsDto>> peopleGroupsAgileMembershipsEmailEnricher(WFIService wfiService) {
        return new PeopleGroupsAgileMembershipsEmailEnricher(wfiService);
    }

    @Bean
    public Enricher<List<PeopleGroupsAgileMembershipsDto>> peopleGroupsAgileMembershipsNameEnricher(AnyOrgClient anyOrgClient, SecureM2MJwtService secureM2MJwtService) {
        return new CompositeEnricher<>(
                new PeopleGroupsAgileMembershipsNameEnricher(anyOrgClient, secureM2MJwtService, anyOrgClientId, anyOrgClientSecret)
        );
    }
}
