package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.batch.CustomMongoItemReader;
import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
@Configuration
public class Step3Config {
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier("mysqlDataSource")
    private final DataSource mysqlDataSource;

    private static final int chunkSize = 4;

    public Step3Config(StepBuilderFactory stepBuilderFactory, MongoTemplate mongoTemplate, DataSource mysqlDataSource) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.mongoTemplate = mongoTemplate;
        this.mysqlDataSource = mysqlDataSource;
    }

    @Bean
    @JobScope
    public Step mongodbToMySQLStep() {
        return stepBuilderFactory.get("mongoDBToMySQL")
            .<Book, Book>chunk(chunkSize)
            .reader(mongoDBItemReader())
            .writer(jdbcItemWriter())
            .build();
    }

    @Bean
    @StepScope
    public CustomMongoItemReader<Book> mongoDBItemReader() {
        CustomMongoItemReader<Book> reader = new CustomMongoItemReader<>(mongoTemplate, "newBook", Book.class);
        reader.setQuery("{}");
        reader.setSort(Collections.singletonMap("created_at", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    public ItemWriter<Book> jdbcItemWriter() {
        JdbcBatchItemWriter<Book> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO sys.books (title, author, publisher, cover_image_url, pages, height, width, thickness, category) " +
                "VALUES (:title, :author, :publisher, :coverImageUrl, :pages, :height, :width, :thickness, :category)");
        writer.setDataSource(mysqlDataSource);
        return writer;
    }
}
