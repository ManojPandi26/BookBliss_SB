package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Books.*;
import com.BookBliss.Entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    BookDetailsDTO getBookById(Long id);

    Page<BookSummaryDTO> getBooksByAuthor(String authorName, Pageable pageable);

    List<BookSummaryDTO> getSimilarBooks(Long bookId, int limit);


    Page<Book> findBooks(Boolean available, Integer yearFrom, Integer yearTo, String categories, Pageable pageable);

    List<BookSearchResponse> searchBooks(String query, int limit);

    List<Book> getRecentlyAddedBooks();

    List<BookSummaryDTO> getRecentBooksPreview();

    // Add new methods for specification-based filtering
    Page<BookSummaryDTO> findBooksByFilter(BookFilterDTO filterDTO, Pageable pageable);

    // Advanced search method
    Page<BookSummaryDTO> searchBooksCriteria(String keyword, Pageable pageable);

}
