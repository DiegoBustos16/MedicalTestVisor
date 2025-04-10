package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
import com.visor.hospital_microservice.service.HospitalDoctorService;
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

import java.util.Optional;

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
            @RequestParam Long doctorId,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Verifica que el doctor exista (a través del client)
        DoctorDTO doctor = doctorClient.getDoctorById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor no encontrado");
        }
        HospitalDoctor hospitalDoctor = new HospitalDoctor();
        hospitalDoctor.setHospitalId(hospitalIdFromJwt);
        hospitalDoctor.setDoctorId(doctorId);

        HospitalDoctor created = hospitalDoctorService.createHospitalDoctor(hospitalDoctor);
        return ResponseEntity.ok(created);
    }

    @Operation(
            summary = "Get Hospital-Doctor Associations",
            description = "Retrieves all hospital-doctor associations for the current user's hospital.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associations retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No active doctor associations found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"No active associations with doctors for this hospital.\"}"
                            )
                    ))
    })
    @GetMapping("")
    public ResponseEntity<?> getHospitalDoctor(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var hospitalDoctors = hospitalDoctorService.getAllHospitalDoctorsByHospitalId(hospitalIdFromJwt);
        if (hospitalDoctors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"No active associations with doctors for this hospital.\"}");
        }
        return ResponseEntity.ok(hospitalDoctors);
    }

    @Operation(
            summary = "Get Hospital-Doctor Association by ID",
            description = "Retrieves a specific hospital-doctor association by its unique ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Association not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Association with ID 100 not found\"}"
                            )
                    ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDoctor> getHospitalDoctorById(@PathVariable Long id) {
        return hospitalDoctorService.getHospitalDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
            @ApiResponse(responseCode = "404", description = "Association not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"No association found for doctorId=1 and hospitalId=2\"}")
                    ))
    })
    @GetMapping("/exist")
    public ResponseEntity<Boolean> existHospitalDoctorByDoctorIdAndHospitalId(
            @RequestParam Long doctorId,
            @RequestParam Long hospitalId) {
        var associations = hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);
        return ResponseEntity.ok(!associations.isEmpty());
    }

    @Operation(
            summary = "Update Hospital-Doctor Association",
            description = "Updates an existing hospital-doctor association. The hospital ID is derived from the current user's Keycloak session, and the doctor information is validated via the DoctorClient.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association updated successfully",
                    content = @Content(mediaType = "application/json")),
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
                    ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospitalDoctor> updateHospitalDoctor(
            @PathVariable Long id,
            @RequestBody HospitalDoctor hospitalDoctor,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Validate doctor existence via DoctorClient
        DoctorDTO doctor = doctorClient.getDoctorById(hospitalDoctor.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("Doctor no encontrado");
        }
        hospitalDoctor.setHospitalId(hospitalIdFromJwt);
        HospitalDoctor updated = hospitalDoctorService.updateHospitalDoctor(id, hospitalDoctor);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Delete Hospital-Doctor Association",
            description = "Deletes a hospital-doctor association by its ID. The association is only deleted if it belongs to the current user's hospital.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Association deleted successfully"),
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
                    ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospitalDoctor(
            @PathVariable Long id,
            Authentication authentication) {
        Optional<HospitalDoctor> associationOpt = hospitalDoctorService.getHospitalDoctorById(id);
        if (associationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HospitalDoctor association = associationOpt.get();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!association.getHospitalId().equals(hospitalIdFromJwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        hospitalDoctorService.deleteHospitalDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
