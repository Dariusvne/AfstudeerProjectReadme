package com.swisscom.travelmate.engine.shared.enrichers.facades;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgileMembershipsEnrichmentFacade {

    private final Enricher<List<AgileMembershipsDto>> fullEnricher;

    public AgileMembershipsEnrichmentFacade(
            @Qualifier("agileMembershipsFullEnricher") Enricher<List<AgileMembershipsDto>> fullEnricher
    ) {
        this.fullEnricher = fullEnricher;
    }

    public List<AgileMembershipsDto> enrichFully(List<AgileMembershipsDto> dtos) {
        return fullEnricher.enrich(dtos);
    }

}