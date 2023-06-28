package com.techeer.checkitbatch.domain.book.entity;

import com.techeer.checkitbatch.global.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "BOOKS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {



    @Id
    @Column(name = "book_id")
    private String id;
    @Column(name = "title")
    private String title;
    @Column(name = "author")
    private String author;
    @Column(name = "publisher")
    private String publisher;
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    @Column(name = "pages")
    private int pages;
    @Column(name = "height")
    private int height;
    @Column(name = "width")
    private int width;
    @Column(name = "thickness")
    private int thickness;
    @Column(name = "category")
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
