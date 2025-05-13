package com.BookBliss.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BookBliss.Entity.Borrowing;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUserId(Long userId);
    boolean existsByBookIdAndStatus(Long bookId, Borrowing.BorrowingStatus status);
    boolean existsByJournalIdAndStatus(Long journalId, Borrowing.BorrowingStatus status);
    
 // Added methods
    long countByUserIdAndStatus(Long userId, Borrowing.BorrowingStatus status);
    boolean existsByUserIdAndStatus(Long userId, Borrowing.BorrowingStatus status);

    Optional<Borrowing> findByUserAndBookAndStatus(User user, Book book, Borrowing.BorrowingStatus status);

    Long countByBookId(Long id);

    @Query("SELECT b FROM Borrowing b WHERE b.book.id = :bookId ORDER BY b.borrowDate DESC")
    Optional<Borrowing> findLatestBorrowingByBookId(@Param("bookId") Long bookId);

    Page<Borrowing> findByStatusOrDueDateBefore(Borrowing.BorrowingStatus borrowingStatus, LocalDateTime now, Pageable pageable);

    long countByStatus(Borrowing.BorrowingStatus borrowingStatus);

    long countByStatusAndDueDateBefore(Borrowing.BorrowingStatus borrowingStatus, LocalDateTime now);


}