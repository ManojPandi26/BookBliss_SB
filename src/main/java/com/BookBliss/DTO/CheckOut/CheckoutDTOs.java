package com.BookBliss.DTO.CheckOut;


import com.BookBliss.DTO.Books.BookShelfDTOs;
import com.BookBliss.Entity.Checkout;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CheckoutDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutRequest {
        @NotNull(message = "Bookshelf ID is required")
        private Long bookshelfId;

        @NotNull(message = "Borrowing days is required")
        @Min(value = 1, message = "Borrowing days must be at least 1")
        private Integer borrowingDays;

        @Size(max = 500, message = "Additional notes cannot exceed 500 characters")
        private String additionalNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutResponse {
        private Long id;
        private Long userId;
        private String username;
        private Long bookshelfId;
        private String checkoutCode;
        private Integer borrowingDays;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;

        private Checkout.CheckoutStatus status;
        private String additionalNotes;
        private BookShelfDTOs.BookshelfResponse bookshelf;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime completedAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime cancelledAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime returnedAt;

        public static CheckoutResponse fromEntity(Checkout checkout) {
            return CheckoutResponse.builder()
                    .id(checkout.getId())
                    .userId(checkout.getUser().getId())
                    .username(checkout.getUser().getUsername())
                    .bookshelfId(checkout.getBookshelf().getId())
                    .checkoutCode(checkout.getCheckoutCode())
                    .borrowingDays(checkout.getBorrowingDays())
                    .dueDate(checkout.getDueDate())
                    .status(checkout.getStatus())
                    .additionalNotes(checkout.getAdditionalNotes())
                    .bookshelf(BookShelfDTOs.BookshelfResponse.fromEntity(checkout.getBookshelf()))
                    .createdAt(checkout.getCreatedAt())
                    .updatedAt(checkout.getUpdatedAt())
                    .completedAt(checkout.getCompletedAt())
                    .cancelledAt(checkout.getCancelledAt())
                    .returnedAt(checkout.getReturnedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private Checkout.CheckoutStatus status;

        @Size(max = 500, message = "Additional notes cannot exceed 500 characters")
        private String additionalNotes;
    }
}
