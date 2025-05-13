package com.BookBliss.Controller;

import com.BookBliss.DTO.ReadingSpace.UserReadingNoteDTO;
import com.BookBliss.Service.ReadingSpace.UserReadingNoteServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class UserReadingNoteController {
    private final UserReadingNoteServiceImpl service;

    @PostMapping
    public ResponseEntity<UserReadingNoteDTO> createNote(
            @Valid @RequestBody UserReadingNoteDTO noteDTO) {
        return new ResponseEntity<>(service.createNote(noteDTO), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<List<UserReadingNoteDTO>> getNotes(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(service.getNotesByUserAndBook(userId, bookId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserReadingNoteDTO> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UserReadingNoteDTO noteDTO) {
        return ResponseEntity.ok(service.updateNote(id, noteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        service.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
