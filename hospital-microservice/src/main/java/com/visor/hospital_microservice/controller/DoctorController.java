package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.service.DoctorService;
import com.visor.hospital_microservice.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private HospitalService hospitalService;

    @Operation(summary = "Read All Doctors", description = "Retrieves all doctors associated to the current user",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Server Error"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/hospital")
    public ResponseEntity<List<DoctorDTO>> getAllDoctorsByHospital(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);
        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(doctorService.getAllDoctorsByHospital(hospitalIdFromJwt));
    }

    @Operation(summary = "Read Doctor by ID", description = "Retrieves a doctor by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }
}