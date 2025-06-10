package com.swisscom.travelmate.engine.shared.enrichers.facades;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PeopleGroupsAgileMembershipsEnrichmentFacade {

    private final Enricher<List<PeopleGroupsAgileMembershipsDto>> fullEnricher;
    private final Enricher<List<PeopleGroupsAgileMembershipsDto>> emailEnricher;
    private final Enricher<List<PeopleGroupsAgileMembershipsDto>> nameEnricher;
    private final Enricher<List<PeopleGroupsAgileMembershipsDto>> userIdEnricher;

    public PeopleGroupsAgileMembershipsEnrichmentFacade(
            @Qualifier("peopleGroupsAgileMembershipsUserIdAndEmailEnricher") Enricher<List<PeopleGroupsAgileMembershipsDto>> fullEnricher,
            @Qualifier("peopleGroupsAgileMembershipsEmailEnricher") Enricher<List<PeopleGroupsAgileMembershipsDto>> emailEnricher,
            @Qualifier("peopleGroupsAgileMembershipsNameEnricher") Enricher<List<PeopleGroupsAgileMembershipsDto>> nameEnricher,
            @Qualifier("peopleGroupsAgileMembershipsUserIdEnricher") Enricher<List<PeopleGroupsAgileMembershipsDto>> userIdEnricher
    ) {
        this.fullEnricher = fullEnricher;
        this.emailEnricher = emailEnricher;
        this.nameEnricher = nameEnricher;
        this.userIdEnricher = userIdEnricher;
    }

    public List<PeopleGroupsAgileMembershipsDto> enrichUserIdAndEmail(List<PeopleGroupsAgileMembershipsDto> dtos) {
        return fullEnricher.enrich(dtos);
    }

    public List<PeopleGroupsAgileMembershipsDto> enrichWithEmail(List<PeopleGroupsAgileMembershipsDto> dtos) {
        return emailEnricher.enrich(dtos);
    }

    public List<PeopleGroupsAgileMembershipsDto> enrichWithNames(List<PeopleGroupsAgileMembershipsDto> dtos) {
        return nameEnricher.enrich(dtos);
    }

    public List<PeopleGroupsAgileMembershipsDto> enrichWithUserId(List<PeopleGroupsAgileMembershipsDto> dtos) {
        return userIdEnricher.enrich(dtos);
    }

}