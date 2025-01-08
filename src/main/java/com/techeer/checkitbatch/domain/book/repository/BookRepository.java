package com.techeer.checkitbatch.domain.book.repository;

import com.techeer.checkitbatch.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByCoverImageUrlAndIsDeletedFalse(String coverImageUrl);
}
