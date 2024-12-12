package com.techeer.checkitbatch.batch;

import com.techeer.checkitbatch.batch.step.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final CrawlingStep crawlingStep;
//    private final DropNewBookStep dropNewBookStep;
//    private final SeleniumToMySQL seleniumToMySQL;
//    private final MoveDataStep moveDataStep;
    private final SetRedisKeyStep setRedisKeyStep;


    @Bean
    public Job job(){
        return jobBuilderFactory.get("Crawling Data Insert DB")
            .start(crawlingStep.crawling()) // 크롤링
//                .next(seleniumToMySQL.seleniumToMySQL())
//                .next(moveDataStep.moveData())
//                .next(dropNewBookStep.dropNewBook())
                .next(setRedisKeyStep.setRedisKey()) // redis에 key 저장
                .build();
    }
}
