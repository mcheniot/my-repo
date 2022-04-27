package com.telus.apiprogram.poc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpringBootBucketApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootBucketApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		System.out.println("==================");
		// if proxy needed to access outside telus, please uncomment these two lines
		System.setProperty("https.proxyHost", "pac.tsl.telus.com");
		System.setProperty("https.proxyPort", "8080");

	}

}
