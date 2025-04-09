package com.visor.test_microservice.client;

import com.visor.test_microservice.configuration.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "hospital-microservice",
        configuration = FeignClientConfig.class
)
public interface HospitalClient {
    @GetMapping("/api/hospitals/hospital-doctor/exist")
    Boolean existHospitalDoctorByDoctorIdAndHospitalId(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("hospitalId") Long hospitalId);
}
