package com.swisscom.travelmate.engine.shared.external.featureflag.model;

public class FlagDto {
    public String key;
    public String reason;
    public boolean value;
    public String variant;

    // Constructor
    public FlagDto() {}

    // Parameterized constructor
    public FlagDto(String key, String reason, boolean value, String variant) {
        this.key = key;
        this.reason = reason;
        this.value = value;
        this.variant = variant;
    }
}
