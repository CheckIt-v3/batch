package com.techeer.checkitbatch.domain.book.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {
    private String id;
    private Long mySqlId;
    private String title;
    private String author;
    private String publisher;
    private String coverImageUrl;
    private int pages;
    private int height;
    private int width;
    private int thickness;
    private String category;
    private LocalDateTime createdAt;

    @Builder
    public Book(String title, String author, String publisher, String coverImageUrl, int pages, int height, int width, int thickness, String category, LocalDateTime createdAt) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.coverImageUrl = coverImageUrl;
        this.pages = pages;
        this.height = height;
        this.width = width;
        this.thickness = thickness;
        this.category = category;
        this.createdAt = createdAt;
    }
}
