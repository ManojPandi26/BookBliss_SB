package com.BookBliss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.BookBliss"})
public class BookBlissWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookBlissWebApplication.class, args);
	}
	
}
