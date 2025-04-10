package com.visor.doctor_microservice.controller;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    @Operation(summary = "Get All Doctors",
            description = "Retrieves a list of all doctors in the system. This endpoint is restricted to authenticated users.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of doctors retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred\"}")
                    ))
    })
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @Operation(summary = "Get Doctor by ID",
            description = "Retrieves a specific doctor by their internal database ID.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Doctor with ID 5 not found\"}")
                    ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Doctor by Keycloak ID",
            description = "Retrieves the doctor ID linked to the given Keycloak user ID (subject claim).",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor ID retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No doctor found for given Keycloak ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"No doctor associated with Keycloak ID abc-123\"}")
                    ))
    })
    @GetMapping("/exist/{keycloakId}")
    public ResponseEntity<Long> getDoctorByKeycloakId(@PathVariable String keycloakId) {
        return doctorService.getDoctorIdByKeycloakId(keycloakId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Doctor by License Number",
            description = "Retrieves a doctor using their professional license number.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Doctor not found for license number",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"No doctor found for license ABC123\"}")
                    ))
    })
    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<Doctor> getDoctorByLicense(@PathVariable String licenseNumber) {
        return doctorService.getDoctorByLicenseNumber(licenseNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update Doctor",
            description = "Updates the authenticated doctor's profile. The doctor is identified by the Keycloak token (`sub` claim).",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request body or token",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Invalid doctor data or missing token\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "Doctor not found for current user")
    })
    @PutMapping
    public ResponseEntity<Doctor> updateDoctor(@RequestBody Doctor doctor) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String keycloakId = jwt.getClaim("sub");
            Long idDoctor = doctorService.findIdbyKeycloak(keycloakId);
            doctor.setId(idDoctor);
            doctor.setIdKeycloak(keycloakId);
            Doctor updatedDoctor = doctorService.updateDoctor(idDoctor, doctor);
            return updatedDoctor != null ? new ResponseEntity<>(updatedDoctor, HttpStatus.OK)
                    : ResponseEntity.notFound().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Delete Doctor",
            description = "Deletes the currently authenticated doctor based on the Keycloak token.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Doctor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found for current user")
    })
    @DeleteMapping
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