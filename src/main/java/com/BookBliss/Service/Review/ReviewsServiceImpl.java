package com.BookBliss.Service.Review;

import com.BookBliss.DTO.Reviews.ReviewsDTO;
import com.BookBliss.DTO.Reviews.ReviewsResponseDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Journal;
import com.BookBliss.Entity.Reviews;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Exception.UserNotFoundException;
import com.BookBliss.Mapper.ReviewsMapper;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.JournalRepository;
import com.BookBliss.Repository.ReviewsRepository;
import com.BookBliss.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewsSection {

    private final ReviewsRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;

    private final ReviewsMapper reviewsMapper;

    @Override
    public ReviewsResponseDTO createReview(ReviewsDTO reviewDTO) {
        Reviews review = new Reviews();

        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        review.setUser(user);

        if (reviewDTO.getBookId() != null) {
            Book book = bookRepository.findById(reviewDTO.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            review.setBook(book);
        }

        if (reviewDTO.getJournalId() != null) {
            Journal journal = journalRepository.findById(reviewDTO.getJournalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Journal not found"));
            review.setJournal(journal);
        }

        if (review.getBook() == null && review.getJournal() == null) {
            throw new InvalidOperationException("Either book or journal must be provided");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        Reviews savedReview = reviewRepository.save(review);
        return reviewsMapper.toResponseDTO(savedReview);
    }

    @Override
    public ReviewsResponseDTO getReview(Long id) {
        Reviews reviews= reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return reviewsMapper.toResponseDTO(reviews);
    }

    @Override
    public List<ReviewsResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewsMapper::toResponseDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<ReviewsResponseDTO> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return reviewRepository.findByUser(user).stream()
                .map(reviewsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewsResponseDTO> getReviewsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        return reviewRepository.findByBook(book).stream()
                .map(reviewsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countReviewsByBook(Long bookId){
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        return reviewRepository.countByBookId(bookId);
    }

    @Override
    public List<ReviewsResponseDTO> getReviewsByJournal(Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new ResourceNotFoundException("Journal not found with id: " + journalId));
        return reviewRepository.findByJournal(journal).stream()
                .map(reviewsMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewsResponseDTO updateReview(Long id, ReviewsDTO reviewDTO) {
        Reviews review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (reviewDTO.getUserId() != null) {
            User user = userRepository.findById(reviewDTO.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + reviewDTO.getUserId()));
            review.setUser(user);
        }

        if (reviewDTO.getBookId() != null) {
            Book book = bookRepository.findById(reviewDTO.getBookId())
                    .orElseThrow(() -> new InvalidOperationException("Book not found with id: " + reviewDTO.getBookId()));
            review.setBook(book);
        }

        if (reviewDTO.getJournalId() != null) {
            Journal journal = journalRepository.findById(reviewDTO.getJournalId())
                    .orElseThrow(() -> new InvalidOperationException("Journal not found with id: " + reviewDTO.getJournalId()));
            review.setJournal(journal);
        }

        if (review.getBook() == null && review.getJournal() == null) {
            throw new InvalidOperationException("Either book or journal must be provided");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        Reviews updatedReview = reviewRepository.save(review);
        return reviewsMapper.toResponseDTO(updatedReview);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Double getAverageBookRating(Long bookId) {
        Double avgRating = reviewRepository.findAverageRatingByBookId(bookId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    @Override
    public Double getAverageJournalRating(Long journalId) {
        Double avgRating = reviewRepository.findAverageRatingByJournalId(journalId);
        return avgRating != null ? avgRating : 0.0;
    }
}
