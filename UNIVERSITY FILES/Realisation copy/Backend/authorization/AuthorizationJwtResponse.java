package com.swisscom.travelmate.engine.shared.external.authorization;

import lombok.Data;

@Data
public class AuthorizationJwtResponse {
    public String token;
    public String access_token;
    public String token_type;
    public Integer expires_in;
    public String scope;


    public AuthorizationJwtResponse(String token, String access_token, String token_type, Integer expires_in, String scope) {
        this.token = token;
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.scope = scope;
    }

    public AuthorizationJwtResponse(){}
}
