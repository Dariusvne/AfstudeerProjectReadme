package com.swisscom.travelmate.engine.shared.monitoring;

import io.prometheus.client.Counter;

public class PrometheusObserver<T> implements Observer<T> {

    private final Counter counter;

    public PrometheusObserver(String metricName, String help) {
        this.counter = Counter.build()
                .name(metricName)
                .help(help)
                .labelNames("type", "status")  // fixed labels
                .register();
    }

    @Override
    public void onSuccess(T payload, String type) {
        counter.labels(type, "success").inc();
    }

    @Override
    public void onFailure(T payload, String type) {
        counter.labels(type, "failure").inc();
    }
}