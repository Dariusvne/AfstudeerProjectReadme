package com.swisscom.travelmate.engine.shared.enrichers.custom.email;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.service.WFIService;

import java.util.List;
import java.util.Optional;

public class PeopleGroupsAgileMembershipsEmailEnricher  implements Enricher<List<PeopleGroupsAgileMembershipsDto>> {

    private final WFIService wfiService;

    public PeopleGroupsAgileMembershipsEmailEnricher(WFIService wfiService) {
        this.wfiService = wfiService;
    }

    @Override
    public List<PeopleGroupsAgileMembershipsDto> enrich(List<PeopleGroupsAgileMembershipsDto> peopleGroupAgileMemberships) {
        List<String> employeeNumbers = peopleGroupAgileMemberships.stream().map(i -> Integer.toString(i.employeeNr)).toList();

        WFIUserResponseDto wfiResponse = wfiService.getUsersByEmployeeNumbers(employeeNumbers);
        peopleGroupAgileMemberships.forEach(peopleGroup -> {
            Optional<WFIUserDto> optionalUser = wfiResponse.getResult().stream().filter(i -> i.getPersonalNumber().equals(Integer.toString(peopleGroup.employeeNr))).findFirst();
            optionalUser.ifPresent(wfiUserDto -> peopleGroup.setEmail(wfiUserDto.getMailInternal()));
        });
        return peopleGroupAgileMemberships;
    }
}
