package com.BookBliss.Controller;

import java.util.List;

import com.BookBliss.DTO.Borrowing.BorrowingStatusResponse;
import com.BookBliss.Exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BookBliss.DTO.Borrowing.BorrowingDTO;
import com.BookBliss.Service.Borrowing.BorrowingServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingServiceImpl borrowingService;

    @PostMapping
    public ResponseEntity<BorrowingDTO> createBorrowing(@Valid @RequestBody BorrowingDTO borrowingDTO) {
        return new ResponseEntity<>(borrowingService.createBorrowing(borrowingDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingDTO> getBorrowingById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.getBorrowingById(id));
    }

    @GetMapping
    public ResponseEntity<List<BorrowingDTO>> getAllBorrowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowingService.getAllBorrowings(page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowingDTO>> getBorrowingsByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByUserId(userId));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowingDTO> returnBorrowing(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.returnBorrowing(id));
    }


    @GetMapping("/check-status")
    public ResponseEntity<BorrowingStatusResponse> checkBorrowingStatus(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        try {
            BorrowingStatusResponse status = borrowingService.checkBorrowingStatus(userId, bookId);
            return ResponseEntity.ok(status);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    //book id not updating...........
    @PutMapping("/{id}")
    public ResponseEntity<BorrowingDTO> updateBorrowing(
            @PathVariable Long id,
            @Valid @RequestBody BorrowingDTO borrowingDTO) {
        return ResponseEntity.ok(borrowingService.updateBorrowing(id, borrowingDTO));
    }

    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long id) {
        borrowingService.deleteBorrowing(id);
        return ResponseEntity.noContent().build();
    }
}
