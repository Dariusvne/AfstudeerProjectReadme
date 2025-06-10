package com.swisscom.travelmate.engine.shared.enrichers.custom.email;

import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;
import com.swisscom.travelmate.engine.shared.util.DepartmentUtils;
import com.swisscom.travelmate.engine.shared.util.UserUtils;

import java.util.Optional;

public class ScaffoldedUserEnricher implements Enricher<User> {

    private final WFIService wfiService;

    public ScaffoldedUserEnricher(WFIService wfiService) {
        this.wfiService = wfiService;
    }

    @Override
    public User enrich(User user) {
        Optional<WFIUserDto> wfiUserDtoOptional = wfiService.getUserByEmployeeNumber(user.getEmployeeNr());

        if(user.isScaffolded() && wfiUserDtoOptional.isPresent()) {
            WFIUserDto wfiUser = wfiUserDtoOptional.get();
            user.updateScaffoldedUser(
                    wfiUser.getFirstName(),
                    wfiUser.getLastName(),
                    null,
                    wfiUser.getMailInternal(),
                    false,
                    null,
                    wfiUser.getOrgOUName(),
                DepartmentUtils.getCountryFromDepartment(wfiUser.getOrgOUName())
            );
        }

        return user;
    }
}