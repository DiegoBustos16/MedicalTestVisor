package com.visor.patient_microservice.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = { @Server(url = "http://localhost:8080")}, info = @Info(title = "Medical Tests API", description = "This lists all the Medical Tests API Calls. The Calls are OAuth2 secured, "
        + "so please use your client ID and Secret to test them out.",
        version = "v1.0"))
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode  = @OAuthFlow(authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}", tokenUrl = "${openapi.oAuthFlow.tokenUrl}", scopes = {
                @OAuthScope(name = "openid", description = "openid scope")
        })))
public class OpenAPI3Configuration {

}

