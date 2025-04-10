package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.service.DoctorService;
import com.visor.hospital_microservice.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    @Operation(
            summary = "Get All Doctors for Hospital",
            description = "Retrieves all doctors associated with the hospital of the current authenticated user. The hospital ID is derived from the current user's Keycloak token.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: The current user is not associated with any doctor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Current user does not have an associated doctors\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected error occurred while retrieving doctors\"}"
                            )
                    )
            )
    })
    @GetMapping("/hospital")
    public ResponseEntity<List<DoctorDTO>> getAllDoctorsByHospital(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<DoctorDTO> doctors = doctorService.getAllDoctorsByHospital(hospitalIdFromJwt);
        return ResponseEntity.ok(doctors);
    }
}
