package com.BookBliss.Repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookshelfItemRepository extends JpaRepository<com.BookBliss.Entity.BookshelfItem, Long> {
    List<com.BookBliss.Entity.BookshelfItem> findByBookshelfId(Long bookshelfId);

    Optional<com.BookBliss.Entity.BookshelfItem> findByBookshelfIdAndBookId(Long bookshelfId, Long bookId);

    @Query("SELECT COUNT(bi) FROM BookshelfItem bi WHERE bi.bookshelf.id = :bookshelfId")
    int countByBookshelfId(@Param("bookshelfId") Long bookshelfId);

    void deleteByBookshelfIdAndBookId(Long bookshelfId, Long bookId);
}
