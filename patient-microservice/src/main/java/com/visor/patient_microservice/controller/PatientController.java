package com.visor.patient_microservice.controller;

import com.visor.patient_microservice.entity.Patient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.visor.patient_microservice.service.PatientService;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @Operation(summary = "Create Patient", description = "Creates a new patient in the system",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping("")
    public ResponseEntity<Patient> createPatient(Patient patient) {
        return ResponseEntity.ok(patientService.createPatient(patient));
    }

    @Operation(summary = "Read All Patients",  description = "Retrieves all patients in the system",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {return ResponseEntity.ok(patientService.getAllPatients());
    }

    @Operation(summary = "Read Patient by ID", description = "Retrieves a patient by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Check if Patient Exists", description = "Checks if a patient exists by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping("/exist/{id}")
    public ResponseEntity<Boolean> existPatientById(@PathVariable Long id) {
        boolean exists = patientService.existPatientById(id);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Search Patients", description = "Search patients by various attributes using QBE",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping("/search")
    public ResponseEntity<List<Patient>> searchPatients(@RequestBody Patient filter) {
        List<Patient> patients = patientService.searchPatients(filter);
        return ResponseEntity.ok(patients);
    }

    @Operation(summary = "Update Patient", description = "Updates a patient in the system",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.updatePatient(id, patient));
    }

    @Operation(summary = "Delete Patient", description = "Deletes a patient in the system",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

}
