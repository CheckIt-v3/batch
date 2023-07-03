package com.techeer.checkitbatch.batch;

import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;
    private final MongoTemplate mongoTemplate;
    private static final int chunkSize = 1000;

    @Bean
    public Job job(Step crawling){
        return jobBuilderFactory.get("Update DB")
                .start(crawling)
                .build();
    }

    @Bean
    @JobScope
    public Step crawling(ItemReader trBookReader){
        return stepBuilderFactory.get("crawling")
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
    public MongoItemReader<Book> trBookReader(MongoOperations mongoTemplate) {
        return new MongoItemReaderBuilder<Book>()
                .name("tweetsItemReader")
//                .targetType(Book.class)
                .jsonQuery("{title : \"임신 출산 육아 대백과\"}")
//                .collection("book")
//                .pageSize(chunkSize)
//                .sorts(Collections.singletonMap("created_at", Sort.Direction.ASC))
                .template(mongoTemplate)
                .targetType(Book.class)
                .build();
    }


//    @Bean
//    public Job simpleJob() {
//        return jobBuilderFactory.get("simpleJob")
//                .start(simpleStep1())
//                .build();
//    }
//
//    @Bean
//    public Step simpleStep1() {
//        return stepBuilderFactory.get("simpleStep1")
//                .tasklet((contribution, chunkContext) -> {
//                    log.info(">>>>> This is Step1");
//                    return RepeatStatus.FINISHED;
//                })
//                .build();
//    }
}
