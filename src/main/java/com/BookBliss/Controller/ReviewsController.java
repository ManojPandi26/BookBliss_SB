package com.BookBliss.Controller;

import com.BookBliss.DTO.Reviews.ReviewsDTO;
import com.BookBliss.DTO.Reviews.ReviewsResponseDTO;
import com.BookBliss.Service.Review.ReviewsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewsServiceImpl reviewService;

    @PostMapping
    public ResponseEntity<ReviewsResponseDTO> createReview(@Valid @RequestBody ReviewsDTO reviewDTO) {
        ReviewsResponseDTO createdReview = reviewService.createReview(reviewDTO);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewsResponseDTO> getReview(@PathVariable Long id) {
        ReviewsResponseDTO reviewsResponseDTO = reviewService.getReview(id);
        return ResponseEntity.ok(reviewsResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<ReviewsResponseDTO>> getAllReviews() {
        List<ReviewsResponseDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewsResponseDTO>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewsResponseDTO> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewsResponseDTO>> getReviewsByBook(@PathVariable Long bookId) {
        List<ReviewsResponseDTO> reviews = reviewService.getReviewsByBook(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/journal/{journalId}")
    public ResponseEntity<List<ReviewsResponseDTO>> getReviewsByJournal(@PathVariable Long journalId) {
        List<ReviewsResponseDTO> reviews = reviewService.getReviewsByJournal(journalId);
        return ResponseEntity.ok(reviews);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReviewsResponseDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewsDTO reviewDTO) {
        ReviewsResponseDTO updatedReview = reviewService.updateReview(id, reviewDTO);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}/average")
    public ResponseEntity<Double> getBookAverageRating(@PathVariable Long bookId) {
        Double averageRating = reviewService.getAverageBookRating(bookId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/journals/{journalId}/average")
    public ResponseEntity<Double> getJournalAverageRating(@PathVariable Long journalId) {
        Double averageRating = reviewService.getAverageJournalRating(journalId);
        return ResponseEntity.ok(averageRating);
    }
}