package com.universidad.vitaltech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VitaltechApplication {

	@org.springframework.beans.factory.annotation.Value("${app.timezone}")
	private String timeZone;

	@jakarta.annotation.PostConstruct
	public void init() {
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(timeZone));
	}

	public static void main(String[] args) {
		SpringApplication.run(VitaltechApplication.class, args);
	}

}
