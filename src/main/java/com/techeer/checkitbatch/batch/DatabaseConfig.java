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
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class DatabaseConfig extends DefaultBatchConfigurer {
    @Override
    public void setDataSource(@Qualifier("mysqlDataSource") DataSource dataSource)
    {
        super.setDataSource(dataSource);
    }


//    @Value("${spring.data.mongodb.uri}")
//    private String mongoUri;
//    @Bean
//    public MongoTemplate mongoDb() {
//        SimpleMongoClientDatabaseFactory databaseFactory = new SimpleMongoClientDatabaseFactory(mongoUri);
//        return new MongoTemplate(databaseFactory);
//    }

    @Bean
    @Qualifier("mysqlDataSource")
    @Primary
    @BatchDataSource
    @ConfigurationProperties("spring.datasource-mysql")
    DataSource mysqlDb(){
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        return builder.build();
    }

    @Bean
    public HashMap<String, String> crawlingMap() {
        return new HashMap<>();
    }
}