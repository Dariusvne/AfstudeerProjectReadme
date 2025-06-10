package com.swisscom.travelmate.engine.shared.enrichers.custom.userid;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.repository.UserRepository;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;

import java.util.List;
import java.util.Optional;

public class AgileMembershipsUserIdEnricher implements Enricher<List<AgileMembershipsDto>> {

    private final UserRepository userRepository;
    private final UserService userService;

    public AgileMembershipsUserIdEnricher(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public List<AgileMembershipsDto> enrich(List<AgileMembershipsDto> agileMembershipsDtos) {
        return agileMembershipsDtos.stream().peek(dto -> {
            String employeeNr = dto.employeeNr;
            Optional<User> user = userRepository.findByEmployeeNr(employeeNr);

            if (user.isEmpty()) {
                User scaffolded = userService.initializeScaffoldedUser(employeeNr);
                dto.setInternalUserId(scaffolded.getId());
            } else {
                dto.setInternalUserId(user.get().getId());
            }}
        ).toList();
    }
}
