package com.BookBliss.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BookBliss.Entity.Journal;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {
    Page<Journal> findByPublisherContainingIgnoreCase(String publisher, Pageable pageable);
    Page<Journal> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    @Query("SELECT j FROM Journal j WHERE " +
           "(:publisher IS NULL OR LOWER(j.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Journal> findByPublisherAndTitleContaining(
        @Param("publisher") String publisher,
        @Param("title") String title,
        Pageable pageable);
}
