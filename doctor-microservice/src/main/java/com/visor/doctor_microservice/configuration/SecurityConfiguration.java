package com.visor.doctor_microservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final DoctorValidationFilter doctorValidationFilter;

    public SecurityConfiguration(DoctorValidationFilter doctorValidationFilter) {
        this.doctorValidationFilter = doctorValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/doctors/swagger-ui/**",
                                "/api/doctors/v3/api-docs/**",
                                "/api/doctors/swagger-resources/**",
                                "/api/doctors/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .addFilterBefore(doctorValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
