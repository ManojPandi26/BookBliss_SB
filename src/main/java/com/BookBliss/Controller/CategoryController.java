package com.BookBliss.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.BookBliss.Entity.Category;
import com.BookBliss.Service.Category.CategoryServiceImpl;

@RestController
@RequestMapping("/api/v1/books/categories")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;


    /**
     * Get all categories.
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Get category by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Get category by name.
     */
    @GetMapping("/name")
    public ResponseEntity<Category> getCategoryByName(@RequestParam String name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }
    
    /**
     * Get categories with book count greater than a value.
     */
    @GetMapping("/filter/book-count")
    public ResponseEntity<List<Category>> getCategoriesWithBookCountGreaterThan(@RequestParam int count) {
        return ResponseEntity.ok(categoryService.getCategoriesWithBookCountGreaterThan(count));
    }

}

