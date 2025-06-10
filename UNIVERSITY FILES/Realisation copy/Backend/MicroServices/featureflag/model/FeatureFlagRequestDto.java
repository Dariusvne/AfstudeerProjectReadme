package com.swisscom.travelmate.engine.shared.external.featureflag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeatureFlagRequestDto {

    @JsonProperty("context")
    private FeatureFlagContextDto context;

    public FeatureFlagRequestDto(FeatureFlagContextDto context) {
        this.context = context;
    }

    public FeatureFlagContextDto getContext() {
        return context;
    }

    public void setContext(FeatureFlagContextDto context) {
        this.context = context;
    }
}