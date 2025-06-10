package com.swisscom.travelmate.engine.shared.external.featureflag.model;

import java.util.List;

public class FeatureFlagResponseDto {
    public List<FlagDto> flags;

    // Constructor
    public FeatureFlagResponseDto() {}

    // Parameterized constructor
    public FeatureFlagResponseDto(List<FlagDto> flags) {
        this.flags = flags;
    }
}
