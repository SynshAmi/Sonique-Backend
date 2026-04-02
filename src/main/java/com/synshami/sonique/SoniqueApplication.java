package com.synshami.sonique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SoniqueApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoniqueApplication.class, args);
	}

}
