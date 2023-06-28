package com.techeer.checkitbatch;

import com.techeer.checkitbatch.domain.book.repository.BookRepository;
import com.techeer.checkitbatch.domain.selenium.Selenium;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackageClasses = BookRepository.class)
@EnableJpaRepositories(excludeFilters =
@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = BookRepository.class))
@SpringBootApplication
public class CheckitbatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckitbatchApplication.class, args).getBean(Selenium.class).crawling();
	}

}
