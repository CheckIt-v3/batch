package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Step1Config {
    private final StepBuilderFactory stepBuilderFactory;
    private static final int chunkSize = 1000;
    private final ItemReader<Book> trBookReader;


    @Bean
    @JobScope
    public Step crawling() {
        return stepBuilderFactory.get("crawling")
            .allowStartIfComplete(true)
            .<Book, Book>chunk(chunkSize)
            .reader(trBookReader)
            .writer(new ItemWriter<Book>() {
                @Override
                public void write(List<? extends Book> items) throws Exception {
                    for (Book book : items) {
                        log.info("********* " + book.getTitle() + " *********");
                    }
                }
            })
            .build();
    }

    @Bean
    @StepScope
    public MongoItemReader<Book> trBookReader(MongoTemplate mongoTemplate) {
        return new MongoItemReaderBuilder<Book>()
            .name("trBookReader")
            .targetType(Book.class)
            .jsonQuery("{}")
            .collection("book")
            .pageSize(chunkSize)
            .sorts(Collections.singletonMap("created_at", Sort.Direction.ASC))
            .template(mongoTemplate)
            .targetType(Book.class)
            .build();
    }
}
