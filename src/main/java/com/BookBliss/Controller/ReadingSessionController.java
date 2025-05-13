package com.BookBliss.Controller;

import com.BookBliss.DTO.ReadingSpace.ReadingSessionDTO;
import com.BookBliss.Service.ReadingSpace.ReadingSessionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reading-sessions")
@RequiredArgsConstructor
public class ReadingSessionController {
    private final ReadingSessionServiceImpl service;

    @PostMapping
    public ResponseEntity<ReadingSessionDTO> createSession(
            @Valid @RequestBody ReadingSessionDTO sessionDTO) {
        return new ResponseEntity<>(service.createSession(sessionDTO), HttpStatus.CREATED);
    }

    @GetMapping("/progress/{progressId}")
    public ResponseEntity<List<ReadingSessionDTO>> getSessionsByProgress(
            @PathVariable Long progressId) {
        return ResponseEntity.ok(service.getSessionsByUserReadingProgress(progressId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadingSessionDTO> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody ReadingSessionDTO sessionDTO) {
        return ResponseEntity.ok(service.updateSession(id, sessionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        service.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}