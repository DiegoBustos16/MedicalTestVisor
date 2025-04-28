package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.service.HospitalDoctorService;
import com.visor.hospital_microservice.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals/hospital-doctor")
public class HospitalDoctorController {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private final DoctorClient doctorClient;

    public HospitalDoctorController(DoctorClient doctorClient) {
        this.doctorClient = doctorClient;
    }

    @Operation(
            summary = "Create Hospital-Doctor Association",
            description = "Creates a new association between a hospital and a doctor. The hospital is determined from the current user's Keycloak session, and the doctor is specified by the 'doctorId' parameter.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hospital-Doctor association created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid doctor ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid doctor ID format\"}"
                            )
                    )),
            @ApiResponse(responseCode = "403", description = "Forbidden. Hospital not found for current user",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Hospital does not exist or user is not authorized\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Doctor not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected error occurred\"}"
                            )
                    ))
    })
    @PostMapping
    public ResponseEntity<HospitalDoctor> createHospitalDoctorAssociation(
            @RequestParam Long doctorId) {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        return ResponseEntity.ok(hospitalDoctorService.createHospitalDoctor(doctorId, hospitalIdFromJwt));
    }

    @Operation(
            summary = "Get Hospital-Doctor Associations",
            description = "Retrieves all hospital-doctor associations for the current user's hospital.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associations retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @GetMapping("")
    public List<HospitalDoctor> getHospitalDoctor() {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        return hospitalDoctorService.getAllHospitalDoctorsByHospitalId(hospitalIdFromJwt);
    }

    @Operation(
            summary = "Get Hospital-Doctor Association by ID",
            description = "Retrieves a specific hospital-doctor association by its unique ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid Association ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid Association ID format\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Association not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Association not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDoctor> getHospitalDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalDoctorService.getHospitalDoctorById(id));
    }

    @Operation(
            summary = "Check Hospital-Doctor Association Existence",
            description = "Checks if a hospital-doctor association exists for a given doctor and hospital pair. Returns true if found, false otherwise.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "true")
                    )),
            @ApiResponse(responseCode = "400", description = "Invalid ID format",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid Doctor or Hospital ID format\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @GetMapping("/exist")
    public ResponseEntity<Boolean> existHospitalDoctorByDoctorIdAndHospitalId(
            @RequestParam Long doctorId,
            @RequestParam Long hospitalId) {
        var associations = hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);
        return ResponseEntity.ok(associations.isPresent());
    }

    @Operation(
            summary = "Update Hospital-Doctor Association",
            description = "Updates an existing hospital-doctor association. The hospital ID is derived from the current user's Keycloak session, and the doctor information is validated via the DoctorClient.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid HospitalDoctor ID",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Invalid Association ID format\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden. Current user not associated with any hospital or unauthorized",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Forbidden: Hospital information not available for current user\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Association not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Association not found for the provided ID\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospitalDoctor> updateHospitalDoctor(
            @PathVariable Long id,
            @Valid @RequestBody HospitalDoctor hospitalDoctor) {

        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        return ResponseEntity.ok(hospitalDoctorService.updateHospitalDoctor(id, hospitalDoctor, hospitalIdFromJwt));
    }

    @Operation(
            summary = "Delete Hospital-Doctor Association",
            description = "Deletes a hospital-doctor association by its ID. The association is only deleted if it belongs to the current user's hospital.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. The association does not belong to the current hospital",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Forbidden: The association does not belong to your hospital\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Association not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Association with provided ID not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HospitalDoctor> deleteHospitalDoctor(
            @PathVariable Long id) {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long hospitalIdFromJwt = hospitalService.getHospitalIdByKeycloakId(keycloakId);

        return ResponseEntity.ok(hospitalDoctorService.deleteHospitalDoctor(id, hospitalIdFromJwt));
    }
}
