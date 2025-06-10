package com.swisscom.travelmate.engine.shared.enrichers.custom.name;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.AuthStyle;
import com.swisscom.travelmate.engine.shared.external.anyorg.client.AnyOrgClient;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationTokenClientConfig;
import com.swisscom.travelmate.engine.shared.external.authorization.SecureM2M.SecureM2MJwtService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PeopleGroupsAgileMembershipsNameEnricher implements Enricher<List<PeopleGroupsAgileMembershipsDto>> {

    private final SecureM2MJwtService secureM2MJwtService;
    private final AnyOrgClient anyOrgClient;
    private final String anyOrgClientId;
    private final String anyOrgClientSecret;

    public PeopleGroupsAgileMembershipsNameEnricher(AnyOrgClient anyOrgClient, SecureM2MJwtService secureM2MJwtService, String anyOrgClientId, String anyOrgClientSecret) {
        this.anyOrgClient = anyOrgClient;
        this.secureM2MJwtService = secureM2MJwtService;
        this.anyOrgClientId = anyOrgClientId;
        this.anyOrgClientSecret = anyOrgClientSecret;

    }

    @Override
    public List<PeopleGroupsAgileMembershipsDto> enrich(List<PeopleGroupsAgileMembershipsDto> peopleGroupsAgileMembershipsDtos) {
        peopleGroupsAgileMembershipsDtos.forEach(peopleGroupAgileMembership ->  {
                List<AgileMembershipsDto> agileMembershipsDtos = anyOrgClient.getAgileMemberships(null, null, null, String.valueOf(peopleGroupAgileMembership.getEmployeeNr()), secureM2MJwtService.getTokenWithBearerPrefix(
                        new AuthorizationTokenClientConfig(anyOrgClientId, anyOrgClientSecret, AuthStyle.Body)
                )).getBody();
                if (agileMembershipsDtos == null) agileMembershipsDtos = new ArrayList<>();
                Optional<AgileMembershipsDto> agileMembership = agileMembershipsDtos.stream().filter(i -> Objects.equals(i.employeeNr, String.valueOf(peopleGroupAgileMembership.getEmployeeNr()))).findFirst();
                if (agileMembership.isPresent()){
                    peopleGroupAgileMembership.setFirstName(agileMembership.get().firstName);
                    peopleGroupAgileMembership.setLastName(agileMembership.get().lastName);
                }
            }
        );
        return peopleGroupsAgileMembershipsDtos;
    }
}