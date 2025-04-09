package com.visor.hospital_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HospitalMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalMicroserviceApplication.class, args);
	}

}
