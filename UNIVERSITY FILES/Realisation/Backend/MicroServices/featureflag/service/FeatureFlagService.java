package com.swisscom.travelmate.engine.shared.external.featureflag.service;

import com.swisscom.travelmate.engine.shared.external.SecureM2MJwtService;
import com.swisscom.travelmate.engine.shared.external.featureflag.client.FeatureFlagClient;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagContextDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagRequestDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FeatureFlagResponseDto;
import com.swisscom.travelmate.engine.shared.external.featureflag.model.FlagDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureFlagService {

    @Value("${featureflag.project.id}")
    private String projectId;

    @Value("${featureflag.environment.id}")
    private String environmentId;

    @Value("${featureflag.targetingkey}")
    private String targetingKey;

    @Value("${featureflag.client.id}")
    private String featureFlagClientId;

    @Value("${featureflag.client.secret}")
    private String featureFlagClientSecret;

    @Autowired
    SecureM2MJwtService secureM2MJwtService;

    @Autowired
    FeatureFlagClient featureFlagClient;

    public List<FlagDto> getAllFeatureFlags() {
        FeatureFlagContextDto featureFlagContext = new FeatureFlagContextDto(projectId, environmentId, targetingKey);
        FeatureFlagRequestDto requestDto = new FeatureFlagRequestDto(featureFlagContext);

        ResponseEntity<List<FlagDto>> response = featureFlagClient.getAllFeatureFlags(
                secureM2MJwtService.getJwtWithBearerPrefix(featureFlagClientSecret, featureFlagClientId),
                requestDto);
        return response.getBody();
    }

    public FlagDto getFeatureFlag(String flagName, String token) {
        ResponseEntity<FlagDto> response = featureFlagClient.getFeatureFlag(flagName, token, new FeatureFlagContextDto(projectId, environmentId, targetingKey));
        return response.getBody();
    }

}