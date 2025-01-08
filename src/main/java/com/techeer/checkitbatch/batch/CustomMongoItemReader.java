//package com.techeer.checkitbatch.batch;
//
//import com.techeer.checkitbatch.domain.book.entity.Book;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.item.data.MongoItemReader;
//import org.springframework.data.mongodb.core.MongoOperations;
//
//@Slf4j
//public class CustomMongoItemReader<T> extends MongoItemReader<T> {
//    public CustomMongoItemReader(MongoOperations template, String collection, Class<? extends T> targetType) {
//        super();
//        this.setTemplate(template);
//        this.setCollection(collection);
//        this.setTargetType(targetType);
//    }
//
//    @Override
//    public T read() throws Exception {
//        T item = super.read();
//        if (item != null) {
//            Book book = (Book) item;
//            log.info("********* {} *********", book.getTitle());
//        }
//        return item;
//    }
//}
//
