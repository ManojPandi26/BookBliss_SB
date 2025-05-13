package com.BookBliss.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.BookBliss.Entity.Book;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    
    /** Find a book by its ISBN. */
    Book findByIsbn(String isbn);

    /** Find books by author. */
    List<Book> findByAuthor(String author);

    /** Find books by title containing a specific keyword (case-insensitive). */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /** Find books by category name. */
    @Query("SELECT b FROM Book b " +
           "JOIN b.categories c " +
           "WHERE c.name = :category")
    List<Book> findByCategory(@Param("category") String category);


    /**  Find books with available copies less than the given threshold   */
    List<Book> findByAvailableCopiesLessThan(int threshold);

    /** Count the total number of books in the database. */
    long count();
    
    //claude dec22
    @Query("SELECT b FROM Book b " +
            "LEFT JOIN b.categories c " +
            "WHERE (:available IS NULL OR (:available = true AND b.availableCopies > 0) " +
            "   OR (:available = false AND b.availableCopies = 0)) " +
            "AND (:yearFrom IS NULL OR b.publicationYear >= :yearFrom) " +
            "AND (:yearTo IS NULL OR b.publicationYear <= :yearTo) " +
            "AND (:categories IS NULL OR c.name IN :categories) " +
            "GROUP BY b")
     Page<Book> findBooksWithFilters(
         @Param("available") Boolean available,
         @Param("yearFrom") Integer yearFrom,
         @Param("yearTo") Integer yearTo,
         @Param("categories") List<String> categories,
         Pageable pageable
     );

     @Query("SELECT b FROM Book b " +
            "WHERE LOWER(b.title) LIKE %:query% " +
            "OR LOWER(b.author) LIKE %:query% " +
            "OR LOWER(b.isbn) LIKE %:query% " +
            "ORDER BY " +
            "CASE " +
            "  WHEN LOWER(b.title) LIKE :query% THEN 1 " +
            "  WHEN LOWER(b.author) LIKE :query% THEN 2 " +
            "  ELSE 3 " +
            "END")
     List<Book> searchBooks(@Param("query") String query, Pageable pageable);

     default List<Book> searchBooks(String query, int limit) {
         return searchBooks(query, Pageable.ofSize(limit));
     }
    
    
     List<Book> findTop5ByOrderByCreatedAtDesc();

    List<Book> findTop8ByOrderByCreatedAtDesc();

     Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
     
     @Query("SELECT DISTINCT b FROM Book b " +
             "LEFT JOIN b.categories c " +
             "WHERE b.id != :bookId " +
             "AND (b.author = :author OR c.name IN :categories) " +
             "ORDER BY " +
             "CASE WHEN b.author = :author THEN 0 ELSE 1 END, " +
             "SIZE(b.categories)")
      List<Book> findSimilarBooks(
              @Param("bookId") Long bookId,
              @Param("author") String author,
              @Param("categories") List<String> categories,
              Pageable pageable);

    @Query(value = """
    SELECT b FROM Book b
    WHERE LOWER(b.title) LIKE %:query%
    OR LOWER(b.author) LIKE %:query%
    OR LOWER(b.isbn) LIKE %:query%
    OR LOWER(b.publisher) LIKE %:query%
    ORDER BY
        CASE
            WHEN LOWER(b.title) = :query THEN 0
            WHEN LOWER(b.title) LIKE :query||'%' THEN 1
            WHEN LOWER(b.title) LIKE '%'||:query||'%' THEN 2
            ELSE 3
        END,
        b.availableCopies DESC
    """)
    List<Book> searchBooksWithFuzzyMatch(@Param("query") String query, Pageable pageable);

    default List<Book> searchBooksWithFuzzyMatch (String query, int limit) {
        return searchBooksWithFuzzyMatch(query, Pageable.ofSize(limit));
    }


    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN categories c " +
            "WHERE c.id = :categoryId")
    Page<Book> findBooksByCategory(Long categoryId, Pageable pageable);

    Page<Book> findByAvailableCopiesLessThanEqual(int threshold, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM Book b WHERE b.id = :bookId AND b.createdAt > :date")
    boolean isBookNew(@Param("bookId") Long bookId, @Param("date") LocalDateTime date);
}

