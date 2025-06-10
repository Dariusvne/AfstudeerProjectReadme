package com.swisscom.travelmate.engine.shared.external.featureflag.api;

import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagContextDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FlagDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.service.FeatureFlagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/featureflags")
public class FeatureFlagController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeatureFlagService featureFlagService;

    @Autowired
    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping("/")
    public ResponseEntity<List<FlagDto>> getAllFeatureFlags() {
        try {
            List<FlagDto> flags = featureFlagService.getAllFeatureFlags();
            return ResponseEntity.ok(flags);

        }catch(Exception e){
            logger.error("Could not fetch user's FeatureFlags");
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{flagName}")
    public ResponseEntity<FlagDto> getFeatureFlag(@RequestHeader("Authorization") String token, @PathVariable String flagName) {
        FlagDto flag = featureFlagService.getFeatureFlag(flagName, token);
        return ResponseEntity.ok(flag);
    }
}