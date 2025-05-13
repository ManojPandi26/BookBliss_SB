package com.BookBliss.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BookBliss.DTO.JournalDTO;
import com.BookBliss.Service.Journal.JournalServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
public class JournalController {
	
	private final JournalServiceImpl journalService;

    @PostMapping
    public ResponseEntity<JournalDTO> createJournal(@Valid @RequestBody JournalDTO journalDTO) {
        return new ResponseEntity<>(journalService.createJournal(journalDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalDTO> getJournalById(@PathVariable Long id) {
        return ResponseEntity.ok(journalService.getJournalById(id));
    }

    @GetMapping
    public ResponseEntity<List<JournalDTO>> getAllJournals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String title) {
        return ResponseEntity.ok(journalService.getAllJournals(page, size, publisher, title));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalDTO> updateJournal(
            @PathVariable Long id,
            @Valid @RequestBody JournalDTO journalDTO) {
        return ResponseEntity.ok(journalService.updateJournal(id, journalDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        journalService.deleteJournal(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/available-copies")
    public ResponseEntity<JournalDTO> updateAvailableCopies(
            @PathVariable Long id,
            @RequestParam int availableCopies) {
        return ResponseEntity.ok(journalService.updateAvailableCopies(id, availableCopies));
    }

}
