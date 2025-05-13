package com.BookBliss.Controller;

import com.BookBliss.DTO.Books.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.BookBliss.Entity.Book;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Service.Book.BookServiceImpl;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookServiceImpl bookService;

    private static final Logger log = LoggerFactory.getLogger(BookController.class);


    @GetMapping
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String categories,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findBooks(available, yearFrom, yearTo, categories, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResponse>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(bookService.searchBooks(q, limit));
    }

    // feb 6
    @GetMapping("/dynamic-search")
    public ResponseEntity<DynamicSearchResponse> searchDynamicBooks(
            @RequestParam String q,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") int limit ) {
        return ResponseEntity.ok(bookService.dynamicSearch(q, userId, limit));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingSearches() {
        try {
            List<String> trendingSearches = bookService.getTrendingSearches();
            return ResponseEntity.ok(trendingSearches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent-searches/{userId}")
    public ResponseEntity<List<String>> getRecentSearches(@PathVariable(required = false) Long userId) {
        try {
            List<String> recentSearches = bookService.getRecentSearches(userId);
            return ResponseEntity.ok(recentSearches);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/recent")
    public ResponseEntity<List<Book>> getRecentlyAddedBooks() {
        List<Book> recentBooks = bookService.getRecentlyAddedBooks();
        return ResponseEntity.ok(recentBooks);
    }


   // Get a book by ID.
     
    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> getBookById(@PathVariable Long id) {
        BookDetailsDTO book = bookService.getBookById(id);
        log.info("Fetched Detailed of the Book"+id);
        return ResponseEntity.ok(book);
    }
    
    // Get books with low availability.
    
//    @GetMapping("/low-availability")
//    public ResponseEntity<List<BookDetailsDTO>> getBooksWithLowAvailability(
//            @RequestParam int threshold) {
//        List<BookDetailsDTO> lowAvailabilityBooks = bookService.getBooksWithLowAvailability(threshold);
//        return ResponseEntity.ok(lowAvailabilityBooks);
//    }
    
    @GetMapping("/by-author/{authorName}")
    public ResponseEntity<Page<BookSummaryDTO>> getBooksByAuthor(
            @PathVariable  String authorName,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "title") String sortBy) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            Page<BookSummaryDTO> books = bookService.getBooksByAuthor(authorName, pageable);
            
            if (books.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field: " + sortBy);
        }
    }
    /**
     * Get similar books recommendations.
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<BookSummaryDTO>> getSimilarBooks(
            @PathVariable  Long id,
            @RequestParam(defaultValue = "5")int limit) {
        try {
            if (limit < 1 || limit > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be between 1 and 20");
            }
            
            List<BookSummaryDTO> similarBooks = bookService.getSimilarBooks(id, limit);
            
            if (similarBooks.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(similarBooks);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/recent-preview")
    public ResponseEntity<List<BookSummaryDTO>> getRecentBookPreview(){
        return ResponseEntity.ok(bookService.getRecentBooksPreview());
    }
    // Need to implement this in FrontEnd

    @GetMapping("/GetBooksCriteria")
    public ResponseEntity<Page<BookSummaryDTO>> getBooksCriteria(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) List<String> categories,
            Pageable pageable) {

        BookFilterDTO filterDTO = BookFilterDTO.builder()
                .available(available)
                .yearFrom(yearFrom)
                .yearTo(yearTo)
                .categories(categories)
                .build();

        return ResponseEntity.ok(bookService.findBooksByFilter(filterDTO, pageable));
    }

    // Add a new, more flexible endpoint for advanced filtering
    @GetMapping("/filter")
    public ResponseEntity<Page<BookSummaryDTO>> filterBooks(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Double averageRatingGreaterThan,
            @RequestParam(required = false) Integer availableCopiesMin,
            @RequestParam(required = false) Integer availableCopiesMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            Pageable pageable) {

        BookFilterDTO filterDTO = BookFilterDTO.builder()
                .available(available)
                .yearFrom(yearFrom)
                .yearTo(yearTo)
                .categories(categories)
                .keyword(keyword)
                .title(title)
                .author(author)
                .isbn(isbn)
                .publisher(publisher)
                .averageRatingGreaterThan(averageRatingGreaterThan)
                .availableCopiesMin(availableCopiesMin)
                .availableCopiesMax(availableCopiesMax)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .build();

        return ResponseEntity.ok(bookService.findBooksByFilter(filterDTO, pageable));
    }

    // Simplified search endpoint for full-text search across multiple fields
    @GetMapping("/criteria-search")
    public ResponseEntity<Page<BookSummaryDTO>> searchBookscriteria(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooksCriteria(keyword, pageable));
    }

    // Optional: Add a POST endpoint for more complex filters
    @PostMapping("/advanced-filter")
    public ResponseEntity<Page<BookSummaryDTO>> advancedFilter(
            @RequestBody BookFilterDTO filterDTO,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findBooksByFilter(filterDTO, pageable));
    }
    
}
