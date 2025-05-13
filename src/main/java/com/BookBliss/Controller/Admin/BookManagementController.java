package com.BookBliss.Controller.Admin;


import com.BookBliss.DTO.Admin.BookManagement.AdminBookDetailsDTO;
import com.BookBliss.DTO.Admin.BookManagement.BookSearchCriteria;
import com.BookBliss.DTO.Books.BookAddingDTO;
import com.BookBliss.Service.Book.BookServiceAdminImpl;
import com.BookBliss.Service.Audit.AuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BookManagementController {

    private final BookServiceAdminImpl bookService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AdminBookDetailsDTO>> getAllBooks(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooksAdmin(pageable));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AdminBookDetailsDTO>> searchBooks(
            @RequestBody BookSearchCriteria searchCriteria,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooksAdmin(searchCriteria, pageable));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<AdminBookDetailsDTO> getBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookByIdAdmin(bookId));
    }

    @PostMapping("/add_book")
    public ResponseEntity<AdminBookDetailsDTO> addBook(@RequestBody @Valid BookAddingDTO bookAddingDTO) {
        AdminBookDetailsDTO savedBook = bookService.addBookAdmin(bookAddingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @PostMapping("/bulk_add_books")
    public ResponseEntity<List<AdminBookDetailsDTO>> addBooks(@RequestBody @Valid List<BookAddingDTO> bookAddingDTOs) {
        List<AdminBookDetailsDTO> addedBooks = bookService.addBooksAdmin(bookAddingDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedBooks);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<AdminBookDetailsDTO> updateBook(
            @PathVariable Long bookId,
            @RequestBody @Valid BookAddingDTO updatedBookDTO) {
        AdminBookDetailsDTO updatedBook = bookService.updateBookAdmin(bookId, updatedBookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok().build();
    }

//    @PatchMapping("/{bookId}/status")
//    public ResponseEntity<AdminBookDetailsDTO> updateBookStatus(
//            @PathVariable Long bookId,
//            @Valid @RequestBody BookStatusUpdateDTO statusUpdateDTO) {
//        return ResponseEntity.ok(bookService.updateBookStatus(bookId, statusUpdateDTO));
//    }

    @PatchMapping("/{bookId}/increment")
    public ResponseEntity<AdminBookDetailsDTO> incrementAvailableCopies(
            @PathVariable Long bookId,
            @RequestParam int incrementBy) {
        return ResponseEntity.ok(bookService.incrementAvailableCopiesAdmin(bookId, incrementBy));
    }

    @PatchMapping("/{bookId}/decrement")
    public ResponseEntity<AdminBookDetailsDTO> decrementAvailableCopies(
            @PathVariable Long bookId,
            @RequestParam int decrementBy) {
        return ResponseEntity.ok(bookService.decrementAvailableCopiesAdmin(bookId, decrementBy));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<Page<AdminBookDetailsDTO>> getBooksByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByCategoryAdmin(categoryId, pageable));
    }

    @GetMapping("/low-availability")
    public ResponseEntity<Page<AdminBookDetailsDTO>> getBooksWithLowAvailability(
            @RequestParam(defaultValue = "5") int threshold,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksWithLowAvailabilityAdmin(threshold, pageable));
    }

//    @GetMapping("/{bookId}/audit-logs")
//    public ResponseEntity<?> getBookAuditLogs(
//            @PathVariable Long bookId,
//            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
//        return ResponseEntity.ok(auditService.getBookAuditLogs(bookId, pageable));
//    }

//    @PostMapping("/{bookId}/restore")
//    public ResponseEntity<AdminBookDetailsDTO> restoreBook(@PathVariable Long bookId) {
//        return ResponseEntity.ok(bookService.restoreBook(bookId));
//    }

//    @GetMapping("/deleted")
//    public ResponseEntity<Page<AdminBookDetailsDTO>> getDeletedBooks(
//            @PageableDefault(size = 20) Pageable pageable) {
//        return ResponseEntity.ok(bookService.getDeletedBooks(pageable));
//    }

//    @GetMapping("/statistics")
//    public ResponseEntity<?> getBookStatistics() {
//        return ResponseEntity.ok(bookService.getBookStatistics());
//    }
}