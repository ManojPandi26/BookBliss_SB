package com.BookBliss.DTO.Books;


import com.BookBliss.Entity.BookshelfItem;
import com.BookBliss.Entity.MyBookshelf;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class BookShelfDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookshelfRequest {
        @NotNull(message = "User ID is required")
        private Long userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookshelfItemRequest {
        @NotNull(message = "Book ID is required")
        private Long bookId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookshelfResponse {
        private Long id;
        private Long userId;
        private String username;
        private MyBookshelf.BookshelfStatus status;
        private Set<BookshelfItemResponse> items;
        private int totalItems;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        public static BookshelfResponse fromEntity(MyBookshelf bookshelf) {
            Set<BookshelfItemResponse> itemResponses = bookshelf.getBookshelfItems().stream()
                    .map(BookshelfItemResponse::fromEntity)
                    .collect(Collectors.toSet());

            return BookshelfResponse.builder()
                    .id(bookshelf.getId())
                    .userId(bookshelf.getUser().getId())
                    .username(bookshelf.getUser().getUsername())
                    .status(bookshelf.getStatus())
                    .items(itemResponses)
                    .totalItems(bookshelf.getTotalItems())
                    .createdAt(bookshelf.getCreatedAt())
                    .updatedAt(bookshelf.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookshelfItemResponse {
        private Long id;
        private Long bookId;
        private String bookTitle;
        private String author;
        private String isbn;
        private String coverImageUrl;
        private Integer quantity;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime addedAt;

        public static BookshelfItemResponse fromEntity(BookshelfItem item) {
            return BookshelfItemResponse.builder()
                    .id(item.getId())
                    .bookId(item.getBook().getId())
                    .bookTitle(item.getBook().getTitle())
                    .author(item.getBook().getAuthor())
                    .isbn(item.getBook().getIsbn())
                    .coverImageUrl(item.getBook().getCoverImageUrl())
                    .quantity(item.getQuantity())
                    .addedAt(item.getCreatedAt())
                    .build();
        }
    }
}