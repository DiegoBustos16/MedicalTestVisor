package com.visor.hospital_microservice.controller;

import com.visor.hospital_microservice.entity.HospitalDoctor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.visor.hospital_microservice.service.HospitalDoctorService;

@RestController
@RequestMapping("/api/hospital-doctor")
public class HospitalDoctorController {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Operation(summary = "Create Hospital-Doctor Association", description = "Creates a new association between a hospital and a doctor",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping
    public ResponseEntity<HospitalDoctor> createHospitalDoctorAssociation(@RequestParam Long hospitalId, @RequestParam Long doctorId) {
        HospitalDoctor hospitalDoctor = new HospitalDoctor();
        hospitalDoctor.setHospitalId(hospitalId);
        hospitalDoctor.setDoctorId(doctorId);
        return ResponseEntity.ok(hospitalDoctorService.createHospitalDoctor(hospitalDoctor));
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

    @Operation(summary = "Update Hospital-Doctor Association", description = "Updates an existing hospital-doctor association",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Hospital-Doctor Association not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospitalDoctor> updateHospitalDoctor(@PathVariable Long id, @RequestBody HospitalDoctor hospitalDoctor) {
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
    public ResponseEntity<Void> deleteHospitalDoctor(@PathVariable Long id) {
        hospitalDoctorService.deleteHospitalDoctor(id);
        return ResponseEntity.noContent().build();
    }


}
