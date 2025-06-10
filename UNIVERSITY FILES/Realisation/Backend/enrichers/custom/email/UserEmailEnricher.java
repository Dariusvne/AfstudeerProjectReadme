package com.swisscom.travelmate.engine.shared.enrichers.custom.email;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;

import java.util.Optional;

public class UserEmailEnricher implements Enricher<User> {

    private final WFIService wfiService;

    public UserEmailEnricher(WFIService wfiService) {
        this.wfiService = wfiService;
    }

    @Override
    public User enrich(User user) {
        Optional<WFIUserDto> wfiUserDto = wfiService.getUserByEmployeeNumber(user.getEmployeeNr());
        if (wfiUserDto.isPresent() && user.getEmail() == null) {
            user.setEmail(wfiUserDto.get().getMailInternal());
        }
        return user;
    }
}
