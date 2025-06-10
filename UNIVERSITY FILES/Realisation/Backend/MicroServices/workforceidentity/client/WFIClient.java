package com.swisscom.travelmate.engine.shared.external.workforceidentity.client;

import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

public interface WFIClient {
    @GetMapping(value = "/openidm/endpoint/digitalMarketplace/PersonalIdentity")
    ResponseEntity<WFIUserResponseDto> queryUsers(
            @RequestParam("_queryFilter") String queryFilter,
            @RequestHeader("Authorization") String token,
            @RequestHeader("SCS-Version") Integer scsVersion
    );
}
