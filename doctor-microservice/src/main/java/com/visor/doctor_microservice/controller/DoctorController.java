package com.visor.doctor_microservice.controller;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;

    @Operation(summary = "Read All Doctors",  description = "Retrieves all tests associated to the current user",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        System.out.println("Buscando doctor con id " + id);
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<Doctor> getDoctorByLicense(@PathVariable String licenseNumber) {
        return doctorService.getDoctorByLicenseNumber(licenseNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping()
    public ResponseEntity<Doctor> updateDoctor(@RequestBody Doctor doctor) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String keycloakId = jwt.getClaim("sub");
            Long idDoctor = doctorService.findIdbyKeycloak(keycloakId);
            Doctor updatedDoctor = doctorService.updateDoctor(idDoctor, doctor);
            return updatedDoctor != null ? new ResponseEntity<>(updatedDoctor, HttpStatus.OK)
                : ResponseEntity.notFound().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteDoctor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Jwt jwt = jwtAuth.getToken();
        String keycloakId = jwt.getClaim("sub");
        Long idDoctor = doctorService.findIdbyKeycloak(keycloakId);
        doctorService.deleteDoctor(idDoctor);
        }
        return ResponseEntity.noContent().build();
    }
}