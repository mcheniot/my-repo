package com.telus.apiprogram.poc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpringBootSecretApplication implements CommandLineRunner {
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecretApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		// get secret from env
		System.out.println("secret - MY_USERNAME from env : " + env.getProperty("MY_USERNAME"));
		System.out.println("secret - MY_PASSWORD from env: " + env.getProperty("MY_PASSWORD"));

		System.out.println("==================");
		// get secret from property file
		System.out.println("secret - MY_USERNAME from property file: " + userName);
		System.out.println("secret - MY_PASSWORD from property file: " + password);
	}
	
	@Autowired
	private Environment env;

	@Value("${property.username}")
	private String userName;
	@Value("${property.password}")
	private String password;

	@RequestMapping("/")
	public String home() {
		return "Welcome to API Program World";
	}

	/*
	 * access secret through ENV variables	 * 
	 * @return
	 */
	@GetMapping("/secrets")
	public String getSecret() {
		return "USERNAME: " + userName + "<br>\nPASSWORD: " + password;
	}

	/*
	 * access secret through mounted volume
	 * 
	 * @param path -- the volume that the secret is mounted to
	 * @return
	 */
	@GetMapping("/volume/{path}")
	public List<String> getVolume(@PathVariable("path") String path) {

		//to demonstrate easily, volume mounted at root directory
		path = "/" + path;

		try {
			List<String> files = Files.list(
					new File(path).toPath())
					.filter(Files::exists)
					.map(Path::toString)
					.collect(Collectors.toList());

			files.forEach(System.out::println);

			return files;

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return Arrays.asList("volume not found");
	}

}
