package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.entity.Hospital;
import com.visor.hospital_microservice.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @Operation(
            summary = "Get All Hospitals",
            description = "Retrieves all hospitals associated to the current user. Returns a list of Hospital objects.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hospitals retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected server error while fetching hospitals\"}"
                            )
                    ))
    })
    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(hospitals);
    }

    @Operation(
            summary = "Get Hospital by ID",
            description = "Retrieves a hospital by its unique ID. The ID is the internal database identifier.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hospital found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid hospital ID",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Invalid hospital ID format\"}"))),
            @ApiResponse(responseCode = "404", description = "Hospital not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Hospital with ID 10 not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
            content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))

    })
    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospitalById(
            @PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @Operation(
            summary = "Update Hospital",
            description = "Updates a hospital using the current user's Keycloak session. The hospital ID is obtained from the authenticated user's token. Returns the updated hospital.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hospital updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid hospital data",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"email: must be a valid format\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden. The current user is not associated with any hospital or does not have permission",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Hospital does not exist or access is forbidden\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Hospital not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Hospital with given keycloak ID not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @PatchMapping
    public ResponseEntity<Hospital> updateHospital(
            @Valid @RequestBody Hospital hospital) {

        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        hospital.setId(hospitalIdFromJwt);
        hospital.setIdKeycloak(keycloakId);

        return ResponseEntity.ok(hospitalService.patchHospital(hospitalIdFromJwt, hospital));

    }

    @Operation(
            summary = "Delete Hospital",
            description = "Deletes the hospital associated with the current authenticated user's Keycloak ID. Use this operation to remove the hospital record.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hospital deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. The current user is not associated with any hospital",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Forbidden: Hospital not associated with user\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Hospital not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Hospital not found for given Keycloak ID\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteHospital(Authentication authentication) {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        hospitalService.deleteHospital(hospitalIdFromJwt);
        return ResponseEntity.ok().build();
    }
}
