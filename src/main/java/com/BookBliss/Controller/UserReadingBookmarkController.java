package com.BookBliss.Controller;

import com.BookBliss.DTO.ReadingSpace.UserReadingBookmarkDTO;
import com.BookBliss.Service.ReadingSpace.UserReadingBookmarkServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class UserReadingBookmarkController {
    private final UserReadingBookmarkServiceImpl service;

    @PostMapping
    public ResponseEntity<UserReadingBookmarkDTO> createBookmark(
            @Valid @RequestBody UserReadingBookmarkDTO bookmarkDTO) {
        return new ResponseEntity<>(service.createBookmark(bookmarkDTO), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<List<UserReadingBookmarkDTO>> getBookmarks(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(service.getBookmarksByUserAndBook(userId, bookId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserReadingBookmarkDTO> updateBookmark(
            @PathVariable Long id,
            @Valid @RequestBody UserReadingBookmarkDTO bookmarkDTO) {
        return ResponseEntity.ok(service.updateBookmark(id, bookmarkDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        service.deleteBookmark(id);
        return ResponseEntity.noContent().build();
    }
}
