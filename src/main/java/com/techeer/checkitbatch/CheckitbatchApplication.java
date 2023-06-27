package com.techeer.checkitbatch;

import com.techeer.checkitbatch.crawling.Selenium;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CheckitbatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckitbatchApplication.class, args).getBean(Selenium.class).process();
	}

}
