package com.swisscom.travelmate.engine.shared.monitoring;

public interface Observer<T> {
    void onSuccess(T payload, String type);
    void onFailure(T payload, String type);
}
