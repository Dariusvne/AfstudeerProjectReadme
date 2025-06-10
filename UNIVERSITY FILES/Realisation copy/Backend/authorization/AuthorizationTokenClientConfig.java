package com.swisscom.travelmate.engine.shared.external.authorization;

import com.swisscom.travelmate.engine.shared.external.AuthStyle;

public class AuthorizationTokenClientConfig {
    public String clientId;
    public String clientSecret;
    public String grantType = "client_credentials";
    public AuthStyle authStyle;

    public AuthorizationTokenClientConfig(String clientId, String clientSecret, AuthStyle authStyle) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authStyle = authStyle != null ? authStyle : AuthStyle.Body;
    }
}
