package com.swisscom.travelmate.engine.shared.external.featureflag.client;

import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagContextDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagRequestDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FlagDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface FeatureFlagClient {
    @PostMapping("/ofrep/v1/evaluate/flags")
    public ResponseEntity<List<FlagDto>> getAllFeatureFlags (@RequestHeader("Authorization") String token, @RequestBody() FeatureFlagRequestDto featureFlagContext);

    @PostMapping("/ofrep/v1/evaluate/flags/{flagName}")
    public ResponseEntity<FlagDto> getFeatureFlag (@PathVariable("flagName") String flagName, @RequestHeader("Authorization") String token, @RequestBody() FeatureFlagContextDto featureFlagContext);
}

