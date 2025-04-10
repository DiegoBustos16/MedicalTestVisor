package com.visor.doctor_microservice.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        servers = {@Server(url = "http://localhost:8080")},
        info = @Info(
                title = "Doctor Microservice API",
                version = "v1.0",
                description = "Manages the information of doctors registered in the system. "
                        + "Authenticate using the `swagger-client` client **without client secret**, "
                        + "and then log in with username and password:\n\n"
                        + "**Username:** _<TO_BE_COMPLETED>_\n"
                        + "**Password:** _<TO_BE_COMPLETED>_"
        )
)
@SecurityScheme(
        name = "security_auth",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${openapi.oAuthFlow.authorizationUrl}",
                        tokenUrl = "${openapi.oAuthFlow.tokenUrl}",
                        scopes = {
                                @OAuthScope(name = "openid", description = "openid scope")
                        }
                )
        )
)
public class OpenAPI3Configuration {
}
