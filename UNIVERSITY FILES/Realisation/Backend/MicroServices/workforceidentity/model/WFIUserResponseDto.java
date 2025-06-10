package com.swisscom.travelmate.engine.shared.external.workforceidentity.model;

import lombok.Data;

import java.util.List;

@Data
public class WFIUserResponseDto {
    private List<WFIUserDto> result;
    private int resultCount;
    private String pagedResultsCookie;
    private String totalPagedResultsPolicy;
    private int totalPagedResults;
    private int remainingPagedResults;
}
