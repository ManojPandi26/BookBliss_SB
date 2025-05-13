package com.BookBliss.Controller;

import com.BookBliss.DTO.Books.BookShelfDTOs;
import com.BookBliss.Entity.BookshelfItem;
import com.BookBliss.Entity.MyBookshelf;
import com.BookBliss.Service.Book.BookshelfServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookshelves")
@RequiredArgsConstructor
@Tag(name = "Bookshelf", description = "Bookshelf management APIs")
public class BookshelfController {

    private final BookshelfServiceImpl bookshelfService;

    @GetMapping("/me/active")
    @Operation(summary = "Get current user's active bookshelf")
    public ResponseEntity<BookShelfDTOs.BookshelfResponse> getActiveBookshelf(@RequestParam Long userId) {
        MyBookshelf bookshelf = bookshelfService.getOrCreateActiveBookshelf(userId);
        return ResponseEntity.ok(BookShelfDTOs.BookshelfResponse.fromEntity(bookshelf));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all bookshelves for current user")
    public ResponseEntity<List<BookShelfDTOs.BookshelfResponse>> getUserBookshelves(@RequestParam Long userId) {

        List<MyBookshelf> bookshelves = bookshelfService.getUserBookshelves(userId);

        List<BookShelfDTOs.BookshelfResponse> response = bookshelves.stream()
                .map(BookShelfDTOs.BookshelfResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bookshelf by ID")
    public ResponseEntity<BookShelfDTOs.BookshelfResponse> getBookshelfById(@PathVariable Long id) {
        MyBookshelf bookshelf = bookshelfService.getBookshelfById(id);
        return ResponseEntity.ok(BookShelfDTOs.BookshelfResponse.fromEntity(bookshelf));
    }

    @PostMapping("/me/books")
    @Operation(summary = "Add book to current user's active bookshelf")
    public ResponseEntity<BookShelfDTOs.BookshelfItemResponse> addBookToBookshelf(
            @Valid @RequestBody BookShelfDTOs.BookshelfItemRequest request,
            @RequestParam Long userId) {

        BookshelfItem item = bookshelfService.addBookToBookshelf(userId, request);
        return new ResponseEntity<>(BookShelfDTOs.BookshelfItemResponse.fromEntity(item), HttpStatus.CREATED);
    }

    @GetMapping("/isInBookshelf")
    @Operation(summary = "check whether the Book is in user's bookshelf")
    public ResponseEntity<Boolean> isInBookshelf(@RequestParam Long userId, @RequestParam Long bookId){
        return ResponseEntity.ok(bookshelfService.isInBookshelf(userId, bookId));
    }

    @PutMapping("/{bookshelfId}/books/{bookId}")
    @Operation(summary = "Update book quantity in bookshelf")
    public ResponseEntity<BookShelfDTOs.BookshelfItemResponse> updateBookQuantity(
            @PathVariable Long bookshelfId,
            @PathVariable Long bookId,
            @RequestParam int quantity) {
        BookshelfItem item = bookshelfService.updateBookshelfItemQuantity(bookshelfId, bookId, quantity);
        return ResponseEntity.ok(BookShelfDTOs.BookshelfItemResponse.fromEntity(item));
    }

    @DeleteMapping("/{bookshelfId}/books/{bookId}")
    @Operation(summary = "Remove book from bookshelf")
    public ResponseEntity<Void> removeBookFromBookshelf(
            @PathVariable Long bookshelfId,
            @PathVariable Long bookId) {
        bookshelfService.removeBookFromBookshelf(bookshelfId, bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/clear")
    @Operation(summary = "Clear all items from bookshelf")
    public ResponseEntity<Void> clearBookshelf(@PathVariable Long id) {
        bookshelfService.clearBookshelf(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/count")
    @Operation(summary = "Get count of items in active bookshelf")
    public ResponseEntity<Integer> getActiveBookshelfItemCount(@RequestParam Long userId) {
        int count = bookshelfService.getActiveBookshelfItemCount(userId);
        return ResponseEntity.ok(count);
    }

}
