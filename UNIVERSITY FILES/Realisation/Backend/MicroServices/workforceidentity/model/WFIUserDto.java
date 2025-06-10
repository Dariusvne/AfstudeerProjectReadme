package com.swisscom.travelmate.engine.shared.external.workforceidentity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WFIUserDto {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    private String orgCompany;
    private String orgLineManager;
    private String orgOUName;

    @JsonProperty("personalnumber")
    private String personalNumber;

    private String preferredLanguage;
    private String displayName;
    private String firstName;
    private String lastName;
    private String adrSwisscomCountry;
    private String mailInternal;
    private String orgLineManagerName;
}
