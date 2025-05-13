package com.BookBliss.Repository;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Journal;
import com.BookBliss.Entity.Reviews;
import com.BookBliss.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
    List<Reviews> findByUser(User user);
    List<Reviews> findByBook(Book book);
    List<Reviews> findByJournal(Journal journal);

    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.journal.id = :journalId")
    Double findAverageRatingByJournalId(@Param("journalId") Long journalId);

    Long countByBookId(Long bookId);

    @Query("SELECT COUNT(r) FROM Reviews r WHERE r.book.id = :bookId")
    Long countReviewsByBookId(@Param("bookId") Long bookId);
}
