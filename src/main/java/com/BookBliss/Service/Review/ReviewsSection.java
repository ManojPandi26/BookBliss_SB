package com.BookBliss.Service.Review;

import com.BookBliss.DTO.Reviews.ReviewsDTO;
import com.BookBliss.DTO.Reviews.ReviewsResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewsSection {
    ReviewsResponseDTO createReview(ReviewsDTO reviewDTO);

    ReviewsResponseDTO getReview(Long id);

    List<ReviewsResponseDTO> getAllReviews();

    List<ReviewsResponseDTO> getReviewsByUser(Long userId);

    List<ReviewsResponseDTO> getReviewsByBook(Long bookId);

    Long countReviewsByBook(Long bookId);

    List<ReviewsResponseDTO> getReviewsByJournal(Long journalId);

    ReviewsResponseDTO updateReview(Long id, ReviewsDTO reviewDTO);

    void deleteReview(Long id);

    @Transactional(readOnly = true)
    Double getAverageBookRating(Long bookId);

    @Transactional(readOnly = true)
    Double getAverageJournalRating(Long journalId);
}
