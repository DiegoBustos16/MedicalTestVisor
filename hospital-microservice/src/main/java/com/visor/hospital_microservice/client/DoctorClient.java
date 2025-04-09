package com.visor.hospital_microservice.client;

import com.visor.hospital_microservice.configuration.FeignClientConfig;
import com.visor.hospital_microservice.dto.DoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "doctor-microservice",
        configuration = FeignClientConfig.class
)
public interface DoctorClient {
    @GetMapping("/api/doctors/{id}")
    DoctorDTO getDoctorById(@PathVariable("id") Long id);
}
