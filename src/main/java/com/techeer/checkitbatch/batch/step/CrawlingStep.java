package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.domain.book.entity.Book;
import com.techeer.checkitbatch.domain.selenium.Selenium;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CrawlingStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashMap<String, String> crawlingMap;

    private final MongoTemplate mongoTemplate;
    private static final int chunkSize = 1000;


    @Bean
    @JobScope
    public Step crawling() {
        return stepBuilderFactory.get("crawling")
            .allowStartIfComplete(true)
            .<Book, Book>chunk(chunkSize)
            .reader(trBookReader())
            .writer(trBookWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<Book> trBookReader() {
        Selenium selenium = new Selenium(redisTemplate, crawlingMap);
        selenium.crawling();
        List<Book> list = Selenium.crawledBookList;
        return new ListItemReader<>(list);
    }

    public MongoItemWriter<Book> trBookWriter() {
        return new MongoItemWriterBuilder<Book>()
                .collection("newBook")
                .template(mongoTemplate)
                .build();
    }

}
