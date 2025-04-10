package com.visor.hospital_microservice.configuration;

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
                title = "Hospital Microservice API",
                version = "v1.0",
                description = "Gestiona hospitales y sus relaciones con doctores. "
                        + "Autenticarse usando el cliente `swagger-client` **sin client secret**, "
                        + "y luego iniciar sesión con usuario y contraseña:\n\n"
                        + "**Usuario:** _<COMPLETAR>_\n"
                        + "**Contraseña:** _<COMPLETAR>_"
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
