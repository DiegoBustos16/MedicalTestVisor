package com.visor.test_microservice.client;

import com.visor.test_microservice.configuration.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "patient-microservice",
        configuration = FeignClientConfig.class
)
public interface PatientClient {
    @GetMapping("/api/patients/exist/{id}")
    boolean existPatientById(@PathVariable("id") Long id);
}
