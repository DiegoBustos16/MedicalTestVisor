package com.visor.doctor_microservice.configuration;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.repository.DoctorRepository;
import com.visor.doctor_microservice.service.DoctorService;
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
public class DoctorValidationFilter extends GenericFilterBean {

    private final DoctorRepository doctorRepository;

    private final DoctorService doctorService;
    private static final List<String> HOSPITAL_ALLOWED_PATTERNS = List.of("/api/doctors", "/api/doctors/**");
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public DoctorValidationFilter(DoctorRepository doctorRepository, DoctorService doctorService) {
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

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
            String email = jwt.getClaim("email");
            String firstName = jwt.getClaim("given_name");
            String lastName = jwt.getClaim("family_name");

            boolean isDoctor = jwtAuth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_doctor"));

            boolean isHospital = jwtAuth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_hospital"));

            if (isHospital && !matchesAllowedPatterns(path)) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso restringido.");
                return;
            }

            if (isDoctor) {
                if (!doctorRepository.existsByIdKeycloakAndDeletedAtIsNull(keycloakId)) {
                    if (!doctorRepository.existsByIdKeycloakAndDeletedAtIsNotNull(keycloakId)) {
                        Doctor newDoctor = new Doctor();
                        newDoctor.setIdKeycloak(keycloakId);
                        newDoctor.setEmail(email);
                        newDoctor.setFirstName(firstName);
                        newDoctor.setLastName(lastName);
                        doctorService.createDoctor(newDoctor);
                    } else {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "El usuario ha sido eliminado.");
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean matchesAllowedPatterns(String path) {
        return HOSPITAL_ALLOWED_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
