package com.visor.test_microservice.controller;

import com.visor.test_microservice.client.DoctorClient;
import com.visor.test_microservice.client.HospitalClient;
import com.visor.test_microservice.client.PatientClient;
import com.visor.test_microservice.dto.UpdateTestDTO;
import com.visor.test_microservice.entity.TestEntity;
import com.visor.test_microservice.service.TestService;
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
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping
    public ResponseEntity<?> createTest(@RequestBody TestEntity testEntity, Authentication authentication) {
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
            @ApiResponse(responseCode = "500", description = "Server Error")
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
            @ApiResponse(responseCode = "404", description = "Test not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestById(@PathVariable String id) {
        Optional<TestEntity> test = testService.getTestById(id);
        return test.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Read Test by Passcode", description = "Retrieves a test by passcode",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Test not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
            })
    @GetMapping("/passcode/{passcode}")
    public ResponseEntity<TestEntity> getTestByPasscode(@PathVariable String passcode) {
        Optional<TestEntity> test = testService.getTestByPasscode(passcode);
        return test.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update Test", description = "Updates a test",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Test not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateTest(@PathVariable String id, @RequestBody UpdateTestDTO testEntity) {
        TestEntity updatedTest = testService.updateTestEntity(id, testEntity);
        return updatedTest != null ? new ResponseEntity<>(updatedTest, HttpStatus.OK)
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete Test", description = "Deletes a test by ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Test not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable String id) {
        testService.deleteTestEntity(id);
        return ResponseEntity.noContent().build();
    }
}
