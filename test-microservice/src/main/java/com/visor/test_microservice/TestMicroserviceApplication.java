package com.visor.test_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TestMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestMicroserviceApplication.class, args);
	}

}
