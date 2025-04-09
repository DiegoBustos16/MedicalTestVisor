package com.visor.hospital_microservice.configuration;


import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.repository.HospitalRepository;
import com.visor.hospital_microservice.service.HospitalService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

@Component
public class HospitalValidationFilter extends GenericFilterBean {

    private final HospitalRepository hospitalRepository;
    private final HospitalService hospitalService;

    private static final List<String> DOCTOR_ALLOWED_PATTERNS = List.of("/api/hospitals/hospital-doctor/exist");
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public HospitalValidationFilter(HospitalRepository hospitalRepository, HospitalService hospitalService) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalService = hospitalService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        String path = ((HttpServletRequest) request).getRequestURI();
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html")) {
            chain.doFilter(request, response);
            return;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String keycloakId = jwt.getClaim("sub");
            String name = jwt.getClaim("name");
            String email = jwt.getClaim("email");

            boolean isDoctor = jwtAuth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_doctor"));

            boolean isHospital = jwtAuth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_hospital"));

            if (isDoctor && !matchesAllowedPatterns(path)) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido.");
                return;
            }

            if (isHospital) {
                if (!hospitalRepository.existsByIdKeycloakAndDeletedAtIsNull(keycloakId)) {
                    if (!hospitalRepository.existsByIdKeycloakAndDeletedAtIsNotNull(keycloakId)) {
                        Hospital newHospital = new Hospital();
                        newHospital.setIdKeycloak(keycloakId);
                        newHospital.setName(name);
                        newHospital.setEmail(email);
                        hospitalService.createHospital(newHospital);
                    } else {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "El hospital ha sido eliminado.");
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean matchesAllowedPatterns(String path) {
        return DOCTOR_ALLOWED_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}