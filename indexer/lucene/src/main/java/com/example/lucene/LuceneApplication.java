package com.example.lucene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootApplication
public class LuceneApplication {

	public static void main(String[] args) {

		SpringApplication.run(LuceneApplication.class, args);
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		// Set any additional configuration for the resolver if needed
		resolver.setMaxUploadSize(10 * 1024 * 1024);
		return resolver;
	}

}
