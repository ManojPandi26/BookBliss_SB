package com.BookBliss.Service.Category;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Category;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BookCategoryService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Add a category to a book and update the book count
     */
    public void addCategoryToBook(Long bookId, Long categoryId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        book.addCategory(category);
        bookRepository.save(book);

        // Update book count - use direct SQL for efficiency
        updateCategoryBookCount(categoryId);
    }

    /**
     * Remove a category from a book and update the book count
     */
    public void removeCategoryFromBook(Long bookId, Long categoryId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        book.removeCategory(category);
        bookRepository.save(book);

        // Update book count - use direct SQL for efficiency
        updateCategoryBookCount(categoryId);
    }

    /**
     * Update the book count for a category using SQL
     */
    @Transactional
    public void updateCategoryBookCount(Long categoryId) {
        // Direct SQL update to avoid fetching the collection
        entityManager.createNativeQuery(
                        "UPDATE categories SET book_count = " +
                                "(SELECT COUNT(*) FROM book_categories WHERE category_id = :categoryId) " +
                                "WHERE id = :categoryId"
                )
                .setParameter("categoryId", categoryId)
                .executeUpdate();
    }

    /**
     * Update book counts for all categories
     */
    @Transactional
    public void updateAllCategoryBookCounts() {
        entityManager.createNativeQuery(
                "UPDATE categories c SET book_count = " +
                        "(SELECT COUNT(*) FROM book_categories bc WHERE bc.category_id = c.id)"
        ).executeUpdate();
    }
}

