package com.BookBliss.Mapper;

import com.BookBliss.DTO.*;
import com.BookBliss.DTO.Reviews.BookReviewDTO;
import com.BookBliss.DTO.Reviews.JournalReviewDTO;
import com.BookBliss.DTO.Reviews.ReviewsResponseDTO;
import com.BookBliss.DTO.Reviews.UserReviewDTO;
import com.BookBliss.Entity.Reviews;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ReviewsMapper {

    public ReviewsResponseDTO toResponseDTO(Reviews review) {
        ReviewsResponseDTO dto = new ReviewsResponseDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        // Map User
        if (review.getUser() != null) {
            UserReviewDTO userDTO = new UserReviewDTO();
            userDTO.setId(review.getUser().getId());
            userDTO.setUsername(review.getUser().getUsername());
            userDTO.setFullName(review.getUser().getFullName());
            userDTO.setProfileImageUrl(review.getUser().getProfileImageUrl());
            dto.setUser(userDTO);
        }

        // Map Book
        if (review.getBook() != null) {
            BookReviewDTO bookDTO = new BookReviewDTO();
            bookDTO.setId(review.getBook().getId());
            bookDTO.setTitle(review.getBook().getTitle());
            bookDTO.setAuthor(review.getBook().getAuthor());
            bookDTO.setCoverImageUrl(review.getBook().getCoverImageUrl());

            // Map Categories
            if (review.getBook().getCategories() != null) {
                bookDTO.setCategories(review.getBook().getCategories().stream()
                        .map(category -> {
                            CategoryDTO categoryDTO = new CategoryDTO();
                            categoryDTO.setId(category.getId());
                            categoryDTO.setName(category.getName());
                            return categoryDTO;
                        })
                        .collect(Collectors.toSet()));
            }

            dto.setBook(bookDTO);
        }

        // Map Journal
        if (review.getJournal() != null) {
            JournalReviewDTO journalDTO = new JournalReviewDTO();
            journalDTO.setId(review.getJournal().getId());
            journalDTO.setTitle(review.getJournal().getTitle());
            journalDTO.setPublisher(review.getJournal().getPublisher());
            journalDTO.setCoverImageUrl(review.getJournal().getCoverImageUrl());
            dto.setJournal(journalDTO);
        }

        return dto;
    }
}
