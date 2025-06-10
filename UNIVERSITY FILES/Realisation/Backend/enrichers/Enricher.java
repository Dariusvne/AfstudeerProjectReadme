package com.swisscom.travelmate.engine.shared.enrichers;

public interface Enricher<T> {
    T enrich(T data);
}
