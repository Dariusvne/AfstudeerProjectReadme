package com.swisscom.travelmate.engine.shared.enrichers.custom.email;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;

import java.util.List;
import java.util.Optional;

public class AgileMembershipsEmailEnricher implements Enricher<List<AgileMembershipsDto>> {

    private final WFIService wfiService;

    public AgileMembershipsEmailEnricher(WFIService wfiService) {
        this.wfiService = wfiService;
    }

    @Override
    public List<AgileMembershipsDto> enrich(List<AgileMembershipsDto> agileMembershipsDtos) {
        List<String> employeeNumbers = agileMembershipsDtos.stream().map(i -> i.employeeNr).toList();
        WFIUserResponseDto wfiResponse = wfiService.getUsersByEmployeeNumbers(employeeNumbers);
        agileMembershipsDtos.forEach(peopleGroup -> {
            Optional<WFIUserDto> optionalUser = wfiResponse.getResult().stream().filter(i -> i.getPersonalNumber().equals(peopleGroup.employeeNr)).findFirst();
            optionalUser.ifPresent(wfiUserDto -> peopleGroup.setEmail(wfiUserDto.getMailInternal()));
        });
        return agileMembershipsDtos;
    }
}