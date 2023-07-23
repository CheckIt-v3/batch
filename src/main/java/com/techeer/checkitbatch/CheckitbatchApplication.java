package com.techeer.checkitbatch;

import com.techeer.checkitbatch.domain.selenium.Selenium;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@EnableBatchProcessing
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CheckitbatchApplication {

	public static void main(String[] args) {
//		SpringApplication.run(CheckitbatchApplication.class, args).getBean(Selenium.class).crawling();
		SpringApplication.run(CheckitbatchApplication.class, args);
	}

}
