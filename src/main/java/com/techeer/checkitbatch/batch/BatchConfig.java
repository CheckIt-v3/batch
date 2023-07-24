package com.techeer.checkitbatch.batch;

import com.techeer.checkitbatch.domain.book.entity.Book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;
    private final MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier("mysqlDataSource")
    private final DataSource mysqlDataSource;
    private static final int chunkSize = 1000;

    @Bean
    // feat/#1
    public Job mongodbToMySQLJob() {
        log.info("mongodbToMySQLJob() 시작"); // 로그 추가
        return jobBuilderFactory.get("mongodbToMySQLJob")
                .start(mongodbToMySQLStep())
                .build();
    }

    @Bean
    public Job job(Step crawling){
        return jobBuilderFactory.get("Update DB")
                .start(crawling)
//                .next(crawling)
                .build();
    }


    @Bean
    @JobScope
  // feat/#1
    public Step mongodbToMySQLStep() {
        return stepBuilderFactory.get("mongodbToMySQLStep")
                .<Book, Book>chunk(4)
                .reader(mongodbItemReader())
                .writer(jdbcItemWriter())
                .build();
    }

    @Bean
    @JobScope
    public Step crawling(ItemReader trBookReader){
        return stepBuilderFactory.get("crawling")
                .allowStartIfComplete(true)
                .<Book, Book>chunk(chunkSize)
                .reader(trBookReader)
                .writer(new ItemWriter<Book>() {
                    @Override
                    public void write(List<? extends Book> items) throws Exception {
                        for(Book book : items) {
                            log.info("********* "+book.getTitle()+" *******");
                        }
                    }
                })
                .build();
    }

    @Bean
    @StepScope
// feat/#1
    public CustomMongoItemReader<Book> mongodbItemReader() {
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
