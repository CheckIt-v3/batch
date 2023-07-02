package com.techeer.checkitbatch.domain.book.entity;

import com.techeer.checkitbatch.global.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Getter
@Table(name = "BOOKS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {
    private String id;
    private String title;
    private String author;
    private String publisher;
    private String coverImageUrl;
    private int pages;
    private int height;
    private int width;
    private int thickness;
    private String category;

    @Builder
    public Book(String title, String author, String publisher, String coverImageUrl, int pages, int height, int width, int thickness, String category) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.coverImageUrl = coverImageUrl;
        this.pages = pages;
        this.height = height;
        this.width = width;
        this.thickness = thickness;
        this.category = category;
    }
}
