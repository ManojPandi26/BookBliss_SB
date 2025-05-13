package com.BookBliss.Controller;

import com.BookBliss.DTO.ReadingSpace.UserReadingProgressDTO;
import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import com.BookBliss.Service.ReadingSpace.UserReadingProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reading-progress")
@RequiredArgsConstructor
public class UserReadingProgressController {
    private final UserReadingProgressService service;

    @PostMapping
    public ResponseEntity<UserReadingProgressDTO> createProgress(
            @Valid @RequestBody UserReadingProgressDTO progressDTO) {
        return new ResponseEntity<>(service.createProgress(progressDTO), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<UserReadingProgressDTO>> getProgressByStatus(
            @PathVariable Long userId,
            @PathVariable UserReadingProgress.ReadingStatus status) {
        return ResponseEntity.ok(service.getProgressByUserAndStatus(userId, status));
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<UserReadingProgressDTO> getProgressByUserAndBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(service.getProgressByUserAndBook(userId, bookId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserReadingProgressDTO> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UserReadingProgressDTO progressDTO) {
        return ResponseEntity.ok(service.updateProgress(id, progressDTO));
    }
}