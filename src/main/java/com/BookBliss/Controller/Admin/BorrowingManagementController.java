package com.BookBliss.Controller.Admin;

import com.BookBliss.DTO.Admin.BorrowingManagement.AdminBorrowingDetailsDTO;
import com.BookBliss.DTO.Admin.BorrowingManagement.BorrowingSearchCriteria;
import com.BookBliss.DTO.Admin.BorrowingManagement.BorrowingStatusUpdateDTO;
import com.BookBliss.Service.Audit.AuditService;

import com.BookBliss.Service.Borrowing.BorrowingServiceAdmin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/borrowings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BorrowingManagementController {

    private final BorrowingServiceAdmin borrowingService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AdminBorrowingDetailsDTO>> getAllBorrowings(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(borrowingService.getAllBorrowingsForAdmin(pageable));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AdminBorrowingDetailsDTO>> searchBorrowings(
            @RequestBody BorrowingSearchCriteria searchCriteria,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(borrowingService.searchBorrowings(searchCriteria, pageable));
    }

    @GetMapping("/{borrowingId}")
    public ResponseEntity<AdminBorrowingDetailsDTO> getBorrowingById(@PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.getAdminBorrowingDetails(borrowingId));
    }

    @PutMapping("/{borrowingId}/status")
    public ResponseEntity<AdminBorrowingDetailsDTO> updateBorrowingStatus(
            @PathVariable Long borrowingId,
            @Valid @RequestBody BorrowingStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(borrowingService.updateBorrowingStatus(borrowingId, statusUpdateDTO));
    }

//    @PutMapping("/{borrowingId}/extend")
//    public ResponseEntity<AdminBorrowingDetailsDTO> extendBorrowingDueDate(
//            @PathVariable Long borrowingId,
//            @Valid @RequestBody BorrowingExtensionDTO extensionDTO) {
//        return ResponseEntity.ok(borrowingService.extendBorrowingDueDate(borrowingId, extensionDTO));
//    }

    @PutMapping("/{borrowingId}/return")
    public ResponseEntity<AdminBorrowingDetailsDTO> processBookReturn(
            @PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.processBookReturn(borrowingId));
    }

    @PutMapping("/{borrowingId}/fine")
    public ResponseEntity<AdminBorrowingDetailsDTO> adjustFineAmount(
            @PathVariable Long borrowingId,
            @RequestParam BigDecimal fineAmount) {
        return ResponseEntity.ok(borrowingService.adjustFineAmount(borrowingId, fineAmount));
    }

    @PutMapping("/{borrowingId}/waive-fine")
    public ResponseEntity<AdminBorrowingDetailsDTO> waiveFine(
            @PathVariable Long borrowingId) {
        return ResponseEntity.ok(borrowingService.waiveFine(borrowingId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<Page<AdminBorrowingDetailsDTO>> getOverdueBorrowings(
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(borrowingService.getOverdueBorrowings(pageable));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getBorrowingStatistics() {
        return ResponseEntity.ok(borrowingService.getBorrowingStatistics());
    }

//    @GetMapping("/{borrowingId}/audit-logs")
//    public ResponseEntity<?> getBorrowingAuditLogs(
//            @PathVariable Long borrowingId,
//            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
//        return ResponseEntity.ok(auditService.getBorrowingAuditLogs(borrowingId, pageable));
//    }

    @DeleteMapping("/{borrowingId}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long borrowingId) {
        borrowingService.deleteBorrowingByAdmin(borrowingId);
        return ResponseEntity.ok().build();
    }
}