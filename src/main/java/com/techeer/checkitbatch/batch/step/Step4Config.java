package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.batch.CustomMongoItemReader;
import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Step4Config {
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;

    @Bean
    @JobScope
    public Step moveDataStep() {
        return stepBuilderFactory.get("moveDataStep")
                .<Book, Book>chunk(100)
                .reader(mongoItemReader())
                .writer(mongoItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public CustomMongoItemReader<Book> mongoItemReader() {
        CustomMongoItemReader<Book> reader = new CustomMongoItemReader<>(mongoTemplate, "newBook", Book.class);
        Query query = new Query();
        query.addCriteria(Criteria.where("title").exists(true));
        reader.setQuery(query);
        reader.setSort(Collections.singletonMap("created_at", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    public MongoItemWriter<Book> mongoItemWriter() {
        return new MongoItemWriterBuilder<Book>()
                .template(mongoTemplate)
                .collection("book")
                .build();
    }
}
