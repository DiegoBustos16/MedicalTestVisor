package com.visor.hospital_microservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DoctorDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(example = "ABC123")
    private String licenseNumber;

    @Schema(example = "Diego")
    private String firstName;

    @Schema(example = "Bustos")
    private String lastName;

    @Schema(example = "16-02-2000")
    private LocalDate dateOfBirth;

    @Schema(example = "Male")
    private String gender;

    @Schema(example = "diegombustos16@gmail.com")
    private String email;

    @Schema(example = "+54912345678")
    private String phoneNumber;
}
