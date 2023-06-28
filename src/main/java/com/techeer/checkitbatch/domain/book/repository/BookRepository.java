package com.techeer.checkitbatch.domain.book.repository;

import com.techeer.checkitbatch.domain.book.entity.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
}
