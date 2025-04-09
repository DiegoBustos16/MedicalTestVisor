package com.visor.test_microservice.client;

import com.visor.test_microservice.configuration.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "doctor-microservice",
        configuration = FeignClientConfig.class
)
public interface DoctorClient {
    @GetMapping("/api/doctors/exist/{keycloakId}")
    Long getDoctorByKeycloakId(@PathVariable("keycloakId") String keycloakId);
}
