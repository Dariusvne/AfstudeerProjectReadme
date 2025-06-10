package com.swisscom.travelmate.engine.shared.enrichers.facades;

import com.swisscom.travelmate.engine.shared.enrichers.Enricher;
import com.swisscom.travelmate.engine.modules.user.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserEnrichmentFacade {

    private final Enricher<User> emailEnricher;
    private final Enricher<User> scaffoldedUserEnricher;

    public UserEnrichmentFacade(
            @Qualifier("userEmailEnricher") Enricher<User> emailEnricher,
            @Qualifier("scaffoldedUserEnricher") Enricher<User> scaffoldedUserEnricher
    ) {
        this.emailEnricher = emailEnricher;
        this.scaffoldedUserEnricher = scaffoldedUserEnricher;
    }

    public User enrichWithEmail(User user) {
        return emailEnricher.enrich(user);
    }
    public User scaffoldedUserEnricher(User user) {return scaffoldedUserEnricher.enrich(user); }
}