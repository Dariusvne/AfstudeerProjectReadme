package com.swisscom.travelmate.engine.shared.enrichers;

public class CompositeEnricher<T> implements Enricher<T> {
    private final Enricher<T>[] enrichers;

    @SafeVarargs
    public CompositeEnricher(Enricher<T>... enrichers) {
        this.enrichers = enrichers;
    }

    @Override
    public T enrich(T data) {
        for (Enricher<T> enricher : enrichers) {
            data = enricher.enrich(data);
        }
        return data;
    }
}
