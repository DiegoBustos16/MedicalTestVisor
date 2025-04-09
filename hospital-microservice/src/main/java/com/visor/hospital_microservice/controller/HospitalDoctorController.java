package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.client.DoctorClient;
import com.visor.hospital_microservice.dto.DoctorDTO;
import com.visor.hospital_microservice.entity.HospitalDoctor;
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
import com.visor.hospital_microservice.service.HospitalDoctorService;

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

    @Operation(summary = "Create Hospital-Doctor Association", description = "Creates a new association between a hospital and a doctor",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping
    public ResponseEntity<HospitalDoctor> createHospitalDoctorAssociation(@RequestParam Long doctorId, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        DoctorDTO doctor = doctorClient.getDoctorById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor no encontrado");
        }
        HospitalDoctor hospitalDoctor = new HospitalDoctor();
        hospitalDoctor.setHospitalId(hospitalIdFromJwt);
        hospitalDoctor.setDoctorId(doctorId);

        return ResponseEntity.ok(hospitalDoctorService.createHospitalDoctor(hospitalDoctor));
    }

    @Operation(summary = "Read All Hospital-Doctor", description = "Retrieves all hospital-doctor associated to the user",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
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
                    .body("No hay relaciones activas con doctores para este hospital.");
        }

        return ResponseEntity.ok(hospitalDoctors);
    }

    @Operation(summary = "Read Hospital-Doctor Association by ID", description = "Retrieves a hospital-doctor association by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HospitalDoctor> getHospitalDoctorById(@PathVariable Long id) {
        return hospitalDoctorService.getHospitalDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Check if Hospital-Doctor Association Exists",
            description = "Checks if a hospital-doctor association exists by Doctor and Hospital ID",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
    })
    @GetMapping("/exist")
    public ResponseEntity<Boolean> existHospitalDoctorByDoctorIdAndHospitalId(
            @RequestParam Long doctorId,
            @RequestParam Long hospitalId) {

        var hospitalDoctors = hospitalDoctorService.getHospitalDoctorByDoctorIdAndHospitalIdAndDeletedAtIsNull(doctorId, hospitalId);

        if (hospitalDoctors.isEmpty()) {
            return ResponseEntity.ok(false);
        }

        return ResponseEntity.ok(true);
    }

    @Operation(summary = "Update Hospital-Doctor Association", description = "Updates an existing hospital-doctor association",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospitalDoctor> updateHospitalDoctor(@PathVariable Long id, @RequestBody HospitalDoctor hospitalDoctor, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DoctorDTO doctor = doctorClient.getDoctorById(hospitalDoctor.getDoctorId());
        if (doctor == null) {
            throw new RuntimeException("Doctor no encontrado");
        }
        hospitalDoctor.setHospitalId(hospitalIdFromJwt);
        HospitalDoctor updatedHospitalDoctor = hospitalDoctorService.updateHospitalDoctor(id, hospitalDoctor);
        if (updatedHospitalDoctor != null) {
            return ResponseEntity.ok(updatedHospitalDoctor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Delete Hospital-Doctor Association", description = "Deletes a hospital-doctor association by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospitalDoctor(@PathVariable Long id, Authentication authentication) {
        Optional<HospitalDoctor> hospitalDoctor = hospitalDoctorService.getHospitalDoctorById(id);
        if (hospitalDoctor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long hospitalIdFromJwt = hospitalService.findIdbyKeycloak(keycloakId);

        if (hospitalIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (hospitalDoctor.get().getHospitalId() != hospitalIdFromJwt) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        hospitalDoctorService.deleteHospitalDoctor(id);
        return ResponseEntity.noContent().build();
    }


}
