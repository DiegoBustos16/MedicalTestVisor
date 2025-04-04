package com.visor.doctor_microservice.configuration;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.repository.DoctorRepository;
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
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
public class DoctorValidationFilter extends GenericFilterBean {

    private final DoctorRepository doctorRepository;

    public DoctorValidationFilter(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
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
            String email = jwt.getClaim("email");
            String firstName = jwt.getClaim("given_name");
            String lastName = jwt.getClaim("family_name");

            try {
                if (!doctorRepository.existsByIdKeycloakAndDeletedAtIsNull(keycloakId)) {
                    if(!doctorRepository.existsByIdKeycloakAndDeletedAtIsNotNull(keycloakId)){
                    Doctor newDoctor = new Doctor();
                    newDoctor.setIdKeycloak(keycloakId);
                    newDoctor.setEmail(email);
                    newDoctor.setFirstName(firstName);
                    newDoctor.setLastName(lastName);
                    doctorRepository.save(newDoctor);
                    } else {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "El usuario ha sido eliminado.");
                        return;
                    }
                }

            } catch (NumberFormatException e) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de ID inválido.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}