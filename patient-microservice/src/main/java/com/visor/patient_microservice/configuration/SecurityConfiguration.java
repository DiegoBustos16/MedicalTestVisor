package com.visor.patient_microservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/patients/swagger-ui/**",
                                "/api/patients/v3/api-docs/**",
                                "/api/patients/swagger-resources/**",
                                "/api/patients/webjars/**"
                        ).permitAll()

                        // public endpoints for read a user
                        .requestMatchers(HttpMethod.GET, "/api/patients/{id}").permitAll()
                        // Doctor protected endpoints for managing patients
                        .requestMatchers(HttpMethod.GET, "/api/patients").hasRole("doctor")
                        .requestMatchers(HttpMethod.POST, "/api/patients/**").hasRole("doctor")
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasRole("doctor")
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("doctor")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) return List.of();

            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) return List.of();

            return roles.stream()
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
