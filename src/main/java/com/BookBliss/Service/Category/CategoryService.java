package com.BookBliss.Service.Category;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.BookBliss.DTO.Admin.CategoryManagement.AdminCategoryDetailsDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategoryCreateUpdateDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategorySearchCriteria;
import com.BookBliss.DTO.Admin.CategoryManagement.BookSummaryDTO;
import com.BookBliss.Entity.Category;

public interface CategoryService {

    // Basic CRUD operations
    Category addCategory(Category category);
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category getCategoryByName(String name);
    Category updateCategory(Long id, String newName);
    void deleteCategory(Long id);

    // Book count operations
    List<Category> getCategoriesWithBookCountGreaterThan(int count);
    void incrementBookCount(String categoryName);
    void decrementBookCount(String categoryName);

    // Admin functions
    Page<AdminCategoryDetailsDTO> getAllCategoriesAdmin(Pageable pageable);
    AdminCategoryDetailsDTO getCategoryByIdAdmin(Long id);
    AdminCategoryDetailsDTO createCategory(CategoryCreateUpdateDTO categoryDTO);
    AdminCategoryDetailsDTO updateCategoryAdmin(Long id, CategoryCreateUpdateDTO categoryDTO);
    void deleteCategoryAdmin(Long id);
    Page<AdminCategoryDetailsDTO> searchCategories(CategorySearchCriteria criteria, Pageable pageable);
    Page<BookSummaryDTO> getCategoryBooks(Long categoryId, Pageable pageable);
    Map<String, Object> getCategoryStatistics();
    // AdminCategoryDetailsDTO mergeCategories(CategoryMergeDTO mergeDTO);
}
