package com.swisscom.travelmate.engine.shared.external.featureflag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeatureFlagContextDto {
    @JsonProperty("ProjectUUID")
    private String projectUUID;

    @JsonProperty("EnvironmentUUID")
    private String environmentUUID;

    @JsonProperty("targetingKey")
    private String targetingKey;

    public FeatureFlagContextDto(String projectUUID, String environmentUUID, String targetingKey){
        this.projectUUID = projectUUID;
        this.environmentUUID = environmentUUID;
        this.targetingKey = targetingKey;
    }
}