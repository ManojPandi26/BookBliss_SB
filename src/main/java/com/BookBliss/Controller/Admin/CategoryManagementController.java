package com.BookBliss.Controller.Admin;

import com.BookBliss.DTO.Admin.CategoryManagement.AdminCategoryDetailsDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategoryCreateUpdateDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategoryMergeDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategorySearchCriteria;
import com.BookBliss.Service.Category.CategoryServiceImpl;
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

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryManagementController {

    private final CategoryServiceImpl categoryService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AdminCategoryDetailsDTO>> getAllCategories(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategoriesAdmin(pageable));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AdminCategoryDetailsDTO>> searchCategories(
            @RequestBody CategorySearchCriteria searchCriteria,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(categoryService.searchCategories(searchCriteria, pageable));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<AdminCategoryDetailsDTO> getCategoryById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryByIdAdmin(categoryId));
    }

    @PostMapping
    public ResponseEntity<AdminCategoryDetailsDTO> createCategory(
            @Valid @RequestBody CategoryCreateUpdateDTO categoryDTO) {
        return new ResponseEntity<>(categoryService.createCategory(categoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<AdminCategoryDetailsDTO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryCreateUpdateDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategoryAdmin(categoryId, categoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategoryAdmin(categoryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCategoryStatistics() {
        return ResponseEntity.ok(categoryService.getCategoryStatistics());
    }

    @GetMapping("/{categoryId}/books")
    public ResponseEntity<?> getCategoryBooks(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(categoryService.getCategoryBooks(categoryId, pageable));
    }

    @PostMapping("/merge")
    public ResponseEntity<AdminCategoryDetailsDTO> mergeCategories(
            @RequestBody @Valid CategoryMergeDTO mergeDTO) {
        return ResponseEntity.ok(categoryService.mergeCategories(mergeDTO));
    }

    @PostMapping("/sync-book-counts")
    public ResponseEntity<Void> syncAllCategoryBookCounts() {
        categoryService.updateAllCategoryBookCounts();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{categoryId}/sync-book-count")
    public ResponseEntity<Void> syncCategoryBookCount(@PathVariable Long categoryId) {
        categoryService.updateCategoryBookCount(categoryId);
        return ResponseEntity.ok().build();
    }

    // need to implement......
//    @GetMapping("/{categoryId}/audit-logs")
//    public ResponseEntity<?> getCategoryAuditLogs(
//            @PathVariable Long categoryId,
//            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
//        return ResponseEntity.ok(auditService.getCategoryAuditLogs(categoryId, pageable));
//    }
}