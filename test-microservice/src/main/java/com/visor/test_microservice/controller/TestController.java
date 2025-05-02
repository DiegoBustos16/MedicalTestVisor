package com.visor.test_microservice.controller;

import com.visor.test_microservice.client.DoctorClient;
import com.visor.test_microservice.dto.PatientTestDTO;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.service.TestService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;
    private final DoctorClient doctorClient;

    @Operation(summary = "Create Test", description = "Creates a new test",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Invalid hospital ID format\"}"),
                                    @ExampleObject(
                                            value = "{\"error\": \"Invalid patient ID format\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Doctor does not exist or user is not authorized\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Hospital does not exist or is deleted\"}"),
                                    @ExampleObject(
                                            value = "{\"error\": \"Patient does not exist\"}"),
                                    @ExampleObject(
                                            value = "{\"error\": \"HospitalDoctor association does not exist\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> createTest(@Valid @RequestBody TestEntity testEntity) {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long doctorIdFromJwt = doctorClient.getDoctorByKeycloakId(keycloakId);
        testEntity.setDoctorId(doctorIdFromJwt);

        return ResponseEntity.ok(testService.createTestEntity(testEntity));
    }

    @Operation(summary = "Read All Tests", description = "Retrieves all tests",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<TestEntity>> getAllTests() {
        List<TestEntity> tests = testService.getAllTests();
        return ResponseEntity.ok(tests);
    }

    @Operation(summary = "Read Test by ID", description = "Retrieves a test by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Test not found with given ID\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestById(@PathVariable String id) {
        return ResponseEntity.ok(testService.getTestById(id));
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
            @ApiResponse(responseCode = "400", description = "Invalid test passcode",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Invalid test passcode format\"}"))),
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
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @GetMapping("/passcode/{passcode}")
    public ResponseEntity<PatientTestDTO> getTestByPasscode(@PathVariable String passcode) {
        return ResponseEntity.ok(testService.getPatientTestByPasscode(passcode));
    }

    @Operation(summary = "Update Test", description = "Updates a test",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Invalid hospital ID format\"}"),
                                    @ExampleObject(
                                            value = "{\"error\": \"Invalid patient ID format\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Test not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateTest(@PathVariable String id, @RequestBody @Valid TestEntity testEntity) {
        String keycloakId = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getSubject();
        Long doctorIdFromJwt = doctorClient.getDoctorByKeycloakId(keycloakId);
        testEntity.setDoctorId(doctorIdFromJwt);
        return ResponseEntity.ok(testService.updateTestEntity(id, testEntity));
    }

    @Operation(summary = "Delete Test", description = "Deletes a test by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid test ID",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Invalid test ID format\"}"))),
            @ApiResponse(responseCode = "404", description = "Test not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Test not found with given ID\"}")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<TestEntity> deleteTest(@PathVariable String id) {
        testService.deleteTestEntity(id);
        return ResponseEntity.ok().build();
    }
}
