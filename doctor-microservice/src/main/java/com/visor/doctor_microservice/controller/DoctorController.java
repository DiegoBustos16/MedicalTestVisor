package com.visor.doctor_microservice.controller;

import com.visor.doctor_microservice.entity.Doctor;
import com.visor.doctor_microservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Get All Doctors",
            description = "Retrieves a list of all doctors in the system. This endpoint is restricted to authenticated users.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of doctors retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Internal Server Error\"}")))
    })
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @Operation(summary = "Get Doctor by ID",
            description = "Retrieves a specific doctor by their internal database ID.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found"),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"No active doctor found with ID: 5\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @Operation(summary = "Get Doctor by Keycloak ID",
            description = "Retrieves the doctor ID linked to the given Keycloak user ID (subject claim).",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor ID retrieved"),
            @ApiResponse(responseCode = "404", description = "Doctor not found for Keycloak ID",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"No active doctor found with Keycloak ID: abc-123\"}")))
    })
    @GetMapping("/exist/{keycloakId}")
    public ResponseEntity<Long> getDoctorByKeycloakId(@PathVariable String keycloakId) {
        return ResponseEntity.ok(doctorService.getDoctorIdByKeycloakId(keycloakId));
    }

    @Operation(summary = "Get Doctor by License Number",
            description = "Retrieves a doctor using their professional license number.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found"),
            @ApiResponse(responseCode = "404", description = "Doctor not found for license",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"No active doctor found with license number: ABC123\"}")))
    })
    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<Doctor> getDoctorByLicense(@PathVariable String licenseNumber) {
        return ResponseEntity.ok(doctorService.getDoctorByLicenseNumber(licenseNumber));
    }

    @Operation(summary = "Update Doctor",
            description = "Updates the authenticated doctor's profile. The doctor is identified by the Keycloak token (`sub` claim).",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid doctor data",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"firstName: must not be blank, email: must be a valid format\"}"))),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PatchMapping
    public ResponseEntity<Doctor> updateDoctor(@Valid @RequestBody Doctor doctor) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String keycloakId = jwtAuth.getToken().getSubject();
            Long idDoctor = doctorService.getDoctorIdByKeycloakId(keycloakId);

            doctor.setId(idDoctor);
            doctor.setIdKeycloak(keycloakId);

            return ResponseEntity.ok(doctorService.patchDoctor(idDoctor, doctor));
        }

        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Delete Doctor",
            description = "Deletes the currently authenticated doctor based on the Keycloak token.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Doctor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteDoctor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String keycloakId = jwtAuth.getToken().getSubject();
            Long idDoctor = doctorService.getDoctorIdByKeycloakId(keycloakId);

            doctorService.deleteDoctor(idDoctor);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.badRequest().build();
    }
}
