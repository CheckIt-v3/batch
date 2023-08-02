package com.techeer.checkitbatch.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SetRedisKeyStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashMap<String, String> crawlingMap;

    @Bean
    @JobScope
    public Step setRedisKey() {
        return stepBuilderFactory.get("setRedisKey")
                .allowStartIfComplete(true)
                .tasklet(setKeyToRedis())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet setKeyToRedis() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                for (String key : crawlingMap.keySet()) {
                    redisTemplate.opsForValue().set("id:" + key, "crawled");
                }
                log.info("redis key 설정 완료");
                return RepeatStatus.FINISHED;
            }
        };
    }

}
