package com.techeer.checkitbatch.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;

    @Bean
    public Job job(Step step){
        return jobBuilderFactory.get("Custom Test")
                .start(step)
                .build();
    }

    @Bean
    public Step step(){
        return stepBuilderFactory.get("Custom Step")
                .tasklet((e,c)->{
                    Long jobInstanceId = c.getStepContext().getJobInstanceId();
                    JobInstance jobInstance = jobExplorer.getJobInstance(jobInstanceId);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


}
