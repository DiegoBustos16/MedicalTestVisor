package com.visor.test_microservice.controller;

import com.visor.test_microservice.client.DoctorClient;
import com.visor.test_microservice.client.HospitalClient;
import com.visor.test_microservice.client.PatientClient;
import com.visor.test_microservice.dto.PatientTestDTO;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.service.TestService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private PatientClient patientClient;

    @Autowired
    private DoctorClient doctorClient;

    @Autowired
    private HospitalClient hospitalClient;

    @Operation(summary = "Create Test", description = "Creates a new test",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Test created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "MissingDoctor",
                                            summary = "Doctor not found",
                                            value = "{\"error\": \"Doctor does not exist or is deleted\"}"),
                                    @ExampleObject(name = "InvalidHospital",
                                            summary = "Doctor not in hospital",
                                            value = "{\"error\": \"Doctor does not belong to the hospital\"}"),
                                    @ExampleObject(name = "InvalidPatient",
                                            summary = "Patient not found",
                                            value = "{\"error\": \"Patient does not exist\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected server error\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> createTest(@Valid @RequestBody TestEntity testEntity, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        Long doctorIdFromJwt = doctorClient.getDoctorByKeycloakId(keycloakId);

        if (doctorIdFromJwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doctor does not exist or is deleted");
        }
        testEntity.setDoctorId(doctorIdFromJwt);

        boolean hospitalExist = hospitalClient.existHospitalDoctorByDoctorIdAndHospitalId(testEntity.getDoctorId(),testEntity.getHospitalId());
        if (!hospitalExist) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doctor does not belong to the hospital");
        }

        boolean patientExist = patientClient.existPatientById(testEntity.getPatientId());
        if (!patientExist) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Patient does not exist");
        }

        TestEntity createdTest = testService.createTestEntity(testEntity);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }

    @Operation(summary = "Read All Tests",  description = "Retrieves all tests",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected server error\"}"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<TestEntity>> getAllTests() {
        List<TestEntity> tests = testService.getAllTests();
        return new ResponseEntity<>(tests, HttpStatus.OK);
    }

    @Operation(summary = "Read Test by ID", description = "Retrieves a test by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Test not found with given ID\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestById(@PathVariable String id) {
        Optional<TestEntity> test = testService.getTestById(id);
        return test.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get Test by Passcode",
            description = "Retrieves a complete test by its passcode, including its image stacks with image files and any file attachments. This endpoint is public and does not require user authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n  \"id\": \"tst-001\",\n  \"doctorId\": 1,\n  \"patientId\": 2,\n  \"hospitalId\": 5,\n  \"createdAt\": \"2025-04-09T12:34:56Z\",\n  \"passCode\": \"ABC123\",\n  \"imageStacks\": [\n    {\n      \"id\": \"stk-001\",\n      \"stackName\": \"Femur Joint\",\n      \"createdAt\": \"2025-04-09T12:35:00Z\",\n      \"imageFiles\": [\n         {\"id\": \"img-001\", \"fileUrl\": \"https://s3.amazonaws.com/bucket/image1.jpg\", \"createdAt\": \"2025-04-09T12:35:05Z\"}\n      ]\n    }\n  ],\n  \"fileAttachments\": [\n    {\"id\": \"att-001\", \"fileName\": \"Doctor Report\", \"fileUrl\": \"https://s3.amazonaws.com/bucket/report.pdf\", \"createdAt\": \"2025-04-09T12:36:00Z\"}\n  ]\n}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Test not found for passcode 'ABC123'\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected error occurred while retrieving the test\"}"
                            )
                    )
            )
    })
    @GetMapping("/passcode/{passcode}")
    public ResponseEntity<PatientTestDTO> getTestByPasscode(@PathVariable String passcode) {
        PatientTestDTO patientTestDTO = testService.getPatientTestByPasscode(passcode);
        if (patientTestDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patientTestDTO);
    }

    @Operation(summary = "Update Test", description = "Updates a test",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Test not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateTest(@PathVariable String id, @RequestBody TestEntity testEntity) {
        TestEntity updatedTest = testService.updateTestEntity(id, testEntity);
        return updatedTest != null ? new ResponseEntity<>(updatedTest, HttpStatus.OK)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete Test", description = "Deletes a test by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Test not found with given ID\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable String id) {
        testService.deleteTestEntity(id);
        return ResponseEntity.noContent().build();
    }
}
