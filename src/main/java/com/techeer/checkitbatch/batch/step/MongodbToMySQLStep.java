package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.batch.CustomMongoItemReader;
import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.RequiredArgsConstructor;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongodbToMySQLStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier("mysqlDataSource")
    private final DataSource mysqlDataSource;
    private static final int chunkSize = 100;

    @Bean
    @JobScope
    public Step mongodbToMySQL() {
        return stepBuilderFactory.get("mongoDBToMySQL")
            .<Book, Book>chunk(chunkSize)
            .reader(mongoDBItemReader())
            .writer(jdbcItemWriter())
                .transactionManager(mysqlTransactionManager(mysqlDataSource))
            .build();
    }

    @Bean
    @StepScope
    public CustomMongoItemReader<Book> mongoDBItemReader() {
        CustomMongoItemReader<Book> reader = new CustomMongoItemReader<>(mongoTemplate, "newBook", Book.class);
        Query query = new Query();
        query.addCriteria(Criteria.where("title").exists(true));
        reader.setQuery(query);
        reader.setSort(Collections.singletonMap("created_at", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    @StepScope
    public ItemWriter<Book> jdbcItemWriter() {
        JdbcBatchItemWriter<Book> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO sys.books (title, author, publisher, cover_image_url, pages, height, width, thickness, category, is_deleted, created_at) " +
                "VALUES (:title, :author, :publisher, :coverImageUrl, :pages, :height, :width, :thickness, :category, false, :createdAt)");
        writer.setDataSource(mysqlDataSource);
        return writer;
    }

    public DataSourceTransactionManager mysqlTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
