package com.techeer.checkitbatch.batch;

import com.techeer.checkitbatch.batch.step.Step1Config;
//import com.techeer.checkitbatch.batch.step.Step3Config;
import com.techeer.checkitbatch.batch.step.Step6Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final Step1Config step1Config;
    private final Step6Config step6Config;

//    private final Step3Config step3Config;

    @Bean
    public Job job(){
        return jobBuilderFactory.get("Crawling Data Insert DB")
            .start(step1Config.crawling()) // 크롤링
            //.next() 몽고디비에 크롤링 데이터 저장
//            .next(step3Config.mongodbToMySQLStep())
                .next(step6Config.setRedisKey()) // redis에 key 저장
                .build();
    }

}
