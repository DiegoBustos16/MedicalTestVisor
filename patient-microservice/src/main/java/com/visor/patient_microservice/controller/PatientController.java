package com.visor.patient_microservice.controller;

import com.visor.patient_microservice.entity.Patient;
import com.visor.patient_microservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Operation(
            summary = "Create Patient",
            description = "Creates a new patient in the system. All fields except the ID are required. The ID is auto-generated.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Missing or invalid fields: firstName is required\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    ))
    })
    @PostMapping("")
    public ResponseEntity<Patient> createPatient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient data to create a new patient",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PatientExample",
                                    summary = "Valid Patient",
                                    value = "{\n  \"identificationNumber\": \"42563459\",\n  \"firstName\": \"Diego\",\n  \"lastName\": \"Bustos\",\n  \"dateOfBirth\": \"2000-02-16\",\n  \"gender\": \"Male\",\n  \"email\": \"diegombustos16@gmail.com\",\n  \"phoneNumber\": \"+54912345678\"\n}"
                            )
                    )
            )
            @RequestBody @Valid Patient patient) {
                return ResponseEntity.ok(patientService.createPatient(patient));
    }

    @Operation(
            summary = "Get All Patients",
            description = "Retrieves all patients available in the system.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of patients retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    ))
    })
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @Operation(
            summary = "Get Patient by ID",
            description = "Retrieves a patient by its ID. The ID must be a valid numeric value."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                            value = "{\"error\": \"Invalid patient ID format\"}")

                    )
            ),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Patient with ID 5 not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    ))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(
            @Parameter(description = "Unique ID of the patient", example = "5")
            @PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @Operation(
            summary = "Check if Patient Exists",
            description = "Checks if a patient exists in the system by its ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns true if the patient exists, false otherwise",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "true")
                    )),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid patient ID format\"}")

                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\"}")
                    ))
    })
    @GetMapping("/exist/{id}")
    public ResponseEntity<Boolean> existPatientById(
            @Parameter(description = "Unique ID of the patient", example = "5")
            @PathVariable Long id) {
        return ResponseEntity.ok(patientService.existPatientById(id));
    }

    @Operation(
            summary = "Search Patients",
            description = "Searches for patients based on provided criteria using a Query By Example (QBE) approach.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patients matching the criteria retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid search criteria: field identification number is invalid\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    ))
    })
    @PostMapping("/search")
    public ResponseEntity<List<Patient>> searchPatients(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient fields to filter search results",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"firstName\": \"Diego\", \"lastName\": \"Bustos\" }"
                            )
                    )
            )
            @RequestBody Patient filter) {
        return ResponseEntity.ok(patientService.searchPatients(filter));
    }

    @Operation(
            summary = "Update Patient",
            description = "Updates an existing patient's data in the system.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid patient ID format\"}")

                    )
            ),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Patient with ID 5 not found\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\"}")
                    ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @Parameter(description = "Unique ID of the patient to update", example = "5")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated patient data",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"firstName\": \"Diego\", \"lastName\": \"Bustos\", \"email\": \"diegombustos16@gmail.com\" }"
                            )
                    )
            )
            @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.updatePatient(id, patient));
    }

    @Operation(
            summary = "Delete Patient",
            description = "Deletes an existing patient from the system by ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Invalid patient ID format\"}")

                    )
            ),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Patient with ID 5 not found\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Internal Server Error\"}")
                    ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Unique ID of the patient to delete", example = "5")
            @PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok().build();
    }
}
