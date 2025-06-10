package com.swisscom.travelmate.engine.shared.enrichers.custom.userid;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;

import java.util.List;
import java.util.Optional;

public class PeopleGroupsAgileMembershipsUserIdEnricher implements Enricher<List<PeopleGroupsAgileMembershipsDto>> {
    private final UserRepository userRepository;
    private final UserService userService;

    public PeopleGroupsAgileMembershipsUserIdEnricher(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public List<PeopleGroupsAgileMembershipsDto> enrich(List<PeopleGroupsAgileMembershipsDto> peopleGroupsAgileMembershipsDtos) {
        return peopleGroupsAgileMembershipsDtos.stream().peek(rte -> {
            String employeeNr = String.valueOf(rte.employeeNr);
            Optional<User> user = userRepository.findByEmployeeNr(employeeNr);

            if (user.isEmpty()) {
                User scaffolded = userService.initializeScaffoldedUser(employeeNr);
                rte.setInternalUserId(scaffolded.getId());
            } else {
                rte.setInternalUserId(user.get().getId());
            }}
        ).toList();
    }
}
