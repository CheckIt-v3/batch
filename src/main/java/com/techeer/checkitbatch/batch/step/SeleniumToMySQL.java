//package com.techeer.checkitbatch.batch.step;
//
//import com.techeer.checkitbatch.domain.book.entity.Book;
//import com.techeer.checkitbatch.domain.selenium.Selenium;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.JobScope;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
//import org.springframework.batch.item.database.JdbcBatchItemWriter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.core.MongoTemplate;
//
//import javax.sql.DataSource;
//import java.util.List;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class SeleniumToMySQL {
//    private final StepBuilderFactory stepBuilderFactory;
////    private final MongoTemplate mongoTemplate;
//    private final Selenium seleniumService;
//    @Autowired
//    @Qualifier("mysqlDataSource")
//    private final DataSource mysqlDataSource;
//
//    private static final int chunkSize = 5;
//
//    @Bean
//    @JobScope
//    public Step seleniumToMySQL() {
//        return stepBuilderFactory.get("seleniumToMySQL")
//            .<Book, Book>chunk(chunkSize)
////            .reader(mongoDBItemReader())
//            .reader(seleniumReader())
//            .writer(jdbcItemWriter())
//            .build();
//    }
//
//    @Bean
//    public ItemReader<Book> seleniumReader() {
//        return new ItemReader<>() {
//            private boolean fetched = false;
//
//            @Override
//            public Book read() {
//                if (fetched) {
//                    return null; // 데이터 읽기가 완료되면 null 반환
//                }
//                fetched = true;
//                List<Book> books = seleniumService.crawling(); // Selenium에서 데이터 크롤링
//                log.info("Selenium 크롤링 완료, {}개의 책을 읽음", books.size());
//                return books.isEmpty() ? null : books.remove(0);
//            }
//        };
//    }
//
////    @Bean
////    @StepScope
////    public CustomMongoItemReader<Book> mongoDBItemReader() {
////        CustomMongoItemReader<Book> reader = new CustomMongoItemReader<>(mongoTemplate, "newBook", Book.class);
////        Query query = new Query();
////        query.addCriteria(Criteria.where("title").exists(true));
////        reader.setQuery(query);
////        reader.setSort(Collections.singletonMap("created_at", Sort.Direction.ASC));
////        return reader;
////    }
//
//    @Bean
//    public ItemWriter<Book> jdbcItemWriter() {
//        JdbcBatchItemWriter<Book> writer = new JdbcBatchItemWriter<>();
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
//        writer.setSql("INSERT INTO sys.books_batch (title, author, publisher, cover_image_url, pages, height, width, thickness, category, is_deleted, created_at) " +
//                "VALUES (:title, :author, :publisher, :coverImageUrl, :pages, :height, :width, :thickness, :category, false, :createdAt)");
//        writer.setDataSource(mysqlDataSource);
//        return writer;
//    }
//}
