package com.techeer.checkitbatch.batch.step;

import com.techeer.checkitbatch.batch.StepExecutionLoggerListener;
import com.techeer.checkitbatch.domain.book.entity.Book;
import com.techeer.checkitbatch.domain.book.repository.BookRepository;
import com.techeer.checkitbatch.domain.selenium.Selenium;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CrawlingStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashMap<String, String> crawlingMap;
//    private final BookRepository bookRepository;
    @Qualifier("mysqlDataSource")
    private final DataSource mysqlDataSource;
    private final StepExecutionLoggerListener listener;

//    private final MongoTemplate mongoTemplate;
    private static final int chunkSize = 5;


    @Bean
    @JobScope
    public Step crawling() {
        return stepBuilderFactory.get("crawling")
            .allowStartIfComplete(true)
            .<Book, Book>chunk(chunkSize)
            .reader(trBookReader())
            .writer(jdbcItemWriter())
                .transactionManager(mysqlTransactionManager(mysqlDataSource))
//                .listener(listener)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Book> trBookReader() {
        Selenium selenium = new Selenium(redisTemplate, crawlingMap);
        selenium.bookCrawling();
        List<Book> list = Selenium.crawledBookList;
        return new ListItemReader<>(list);
    }

//    public MongoItemWriter<Book> trBookWriter() {
//        return new MongoItemWriterBuilder<Book>()
//                .collection("newBook")
//                .template(mongoTemplate)
//                .build();
//    }

    @Bean
    @StepScope
    public ItemWriter<Book> jdbcItemWriter() {
        JdbcBatchItemWriter<Book> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO book_test (title, author, publisher, cover_image_url, pages, height, width, thickness, category, is_deleted, created_at) " +
                "VALUES (:title, :author, :publisher, :coverImageUrl, :pages, :height, :width, :thickness, :category, false, :createdAt)");
        writer.setDataSource(mysqlDataSource);
        return writer;
    }

    public DataSourceTransactionManager mysqlTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
