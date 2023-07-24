package com.techeer.checkitbatch.batch;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig extends DefaultBatchConfigurer {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource-h2")
    DataSource springBatchDb(){
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        return builder.build();
    }

    @Override
    public void setDataSource(@Qualifier("h2Datasource") DataSource dataSource)
    {
        // H2 datasource를 batch 기본 datasource로 설정해줍니다.
        super.setDataSource(dataSource);
    }


    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    @Bean
    public MongoTemplate mongoDb() {
        SimpleMongoClientDatabaseFactory databaseFactory = new SimpleMongoClientDatabaseFactory(mongoUri);
        return new MongoTemplate(databaseFactory);
    }

    @Bean
    @BatchDataSource
    @ConfigurationProperties("spring.datasource-mysql")
    DataSource mysqlDb(){
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        return builder.build();
    }



}