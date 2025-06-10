package com.swisscom.travelmate.engine.shared.external.authorization;

public class AuthorizationJwtRequest {
    public String client_secret;
    public String client_id;
    public String grant_type;


    public AuthorizationJwtRequest(String client_secret, String client_id, String grant_type) {
        this.client_secret = client_secret;
        this.client_id = client_id;
        this.grant_type = grant_type;
    }

    public AuthorizationJwtRequest(){}
}
