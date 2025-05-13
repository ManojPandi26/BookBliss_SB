package com.BookBliss.Service.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.BookBliss.Mapper.CategoryMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BookBliss.DTO.Admin.CategoryManagement.AdminCategoryDetailsDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategoryCreateUpdateDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategoryMergeDTO;
import com.BookBliss.DTO.Admin.CategoryManagement.CategorySearchCriteria;
import com.BookBliss.DTO.Admin.CategoryManagement.BookSummaryDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Category;
import com.BookBliss.Exception.DuplicateResourceException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a new category.
     */
    @Override
    public Category addCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + category.getName() + "' already exists.");
        }

        Category newcategory = new Category();
        newcategory.setName(category.getName());
        newcategory.setBookCount(0);// Initialize book count to 0
        newcategory.setDescription(category.getDescription());
        return categoryRepository.save(category);
    }

    /**
     * Get all categories.
     */
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get all categories with pagination for admin panel.
     */
    @Transactional
    public Page<AdminCategoryDetailsDTO> getAllCategoriesAdmin(Pageable pageable) {
        // Make sure book counts are in sync
        updateAllCategoryBookCounts();
        return categoryRepository.findAll(pageable)
                .map(this::convertToAdminCategoryDetailsDTO);
    }

    /**
     * Get a category by ID.
     */
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    /**
     * Get a category by ID for admin panel with detailed information.
     */
    @Transactional
    public AdminCategoryDetailsDTO getCategoryByIdAdmin(Long id) {
        Category category = getCategoryById(id);
        // Update book count before returning
        updateCategoryBookCount(id);
        return convertToAdminCategoryDetailsDTO(category);
    }

    /**
     * Get a category by name.
     */
    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    /**
     * Update a category's name.
     */
    @Override
    @Transactional
    public Category updateCategory(Long id, String newName) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (!category.getName().equals(newName) && categoryRepository.findByName(newName).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + newName + "' already exists.");
        }

        category.setName(newName);
        return categoryRepository.save(category);
    }

    /**
     * Create a new category from DTO.
     */
    @Transactional
    public AdminCategoryDetailsDTO createCategory(CategoryCreateUpdateDTO categoryDTO) {
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + categoryDTO.getName() + "' already exists.");
        }

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .bookCount(0)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return convertToAdminCategoryDetailsDTO(savedCategory);
    }

    /**
     * Update a category for admin panel with DTO.
     */
    @Transactional
    public AdminCategoryDetailsDTO updateCategoryAdmin(Long id, CategoryCreateUpdateDTO categoryDTO) {
        Category category = getCategoryById(id);

        // Check if name is being updated and if it would create a duplicate
        if (!category.getName().equals(categoryDTO.getName()) &&
                categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + categoryDTO.getName() + "' already exists.");
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return convertToAdminCategoryDetailsDTO(updatedCategory);
    }

    /**
     * Delete a category for admin panel.
     */
    @Transactional
    public void deleteCategoryAdmin(Long id) {
        Category category = getCategoryById(id);

        // Update book count before checking
        updateCategoryBookCount(id);
        category = getCategoryById(id); // Refresh the entity

        if (category.getBookCount() > 0) {
            throw new IllegalStateException("Cannot delete category with existing books. Use merge function instead.");
        }

        categoryRepository.delete(category);
    }

    /**
     * Delete a category.
     * Ensure no books are linked to the category before deletion.
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Update book count before checking
        updateCategoryBookCount(id);
        category = getCategoryById(id); // Refresh the entity

        if (category.getBookCount() > 0) {
            throw new IllegalStateException("Cannot delete category with existing books.");
        }

        categoryRepository.delete(category);
    }

    /**
     * Get categories with more than a specified number of books.
     */
    @Override
    @Transactional
    public List<Category> getCategoriesWithBookCountGreaterThan(int count) {
        // Ensure all book counts are accurate
        updateAllCategoryBookCounts();
        return categoryRepository.findByBookCountGreaterThan(count);
    }

    /**
     * Increment the book count for a category.
     */
    @Override
    @Transactional
    public void incrementBookCount(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + categoryName));

        // Use direct SQL update to avoid concurrency issues
        entityManager.createQuery("UPDATE categories c SET c.bookCount = c.bookCount + 1 WHERE c.id = :id")
                .setParameter("id", category.getId())
                .executeUpdate();
    }

    /**
     * Decrement the book count for a category.
     */
    @Override
    @Transactional
    public void decrementBookCount(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + categoryName));

        if (category.getBookCount() == 0) {
            throw new IllegalStateException("Book count cannot be negative.");
        }

        // Use direct SQL update to avoid concurrency issues
        entityManager.createQuery("UPDATE categories c SET c.bookCount = c.bookCount - 1 WHERE c.id = :id AND c.bookCount > 0")
                .setParameter("id", category.getId())
                .executeUpdate();
    }

    /**
     * Update book count for a specific category using the actual relationship table data
     */
    @Transactional
    public void updateCategoryBookCount(Long categoryId) {
        entityManager.createNativeQuery(
                        "UPDATE categories SET book_count = " +
                                "(SELECT COUNT(*) FROM book_categories WHERE category_id = :categoryId) " +
                                "WHERE id = :categoryId"
                )
                .setParameter("categoryId", categoryId)
                .executeUpdate();
    }

    /**
     * Update book counts for all categories using the actual relationship table data
     */
    @Transactional
    public void updateAllCategoryBookCounts() {
        entityManager.createNativeQuery(
                "UPDATE categories c SET book_count = " +
                        "(SELECT COUNT(*) FROM book_categories bc WHERE bc.category_id = c.id)"
        ).executeUpdate();
    }

    /**
     * Search categories based on criteria using JPA Specification.
     */
    @Transactional
    public Page<AdminCategoryDetailsDTO> searchCategories(CategorySearchCriteria criteria, Pageable pageable) {
        // Ensure book counts are accurate
        updateAllCategoryBookCounts();

        Specification<Category> spec = buildCategorySpecification(criteria);
        Page<Category> categories = categoryRepository.findAll(spec, pageable);

        return categories.map(categoryMapper::toAdminDto);
    }

    /**
     * Build a JPA Specification based on search criteria.
     */
    private Specification<Category> buildCategorySpecification(CategorySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            if (criteria.getMinBookCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bookCount"), criteria.getMinBookCount()));
            }

            if (criteria.getMaxBookCount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bookCount"), criteria.getMaxBookCount()));
            }

            if (criteria.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Get books in a category with pagination - improved to use the join table directly.
     */
    public Page<BookSummaryDTO> getCategoryBooks(Long categoryId, Pageable pageable) {
        Category category = getCategoryById(categoryId);

        // Using a more efficient query with the join table
        List<Book> books = entityManager.createQuery(
                        "SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId", Book.class)
                .setParameter("categoryId", categoryId)
                .getResultList();

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());

        List<Book> pageContent = start < end ? books.subList(start, end) : List.of();
        List<BookSummaryDTO> bookDTOs = pageContent.stream()
                .map(this::convertToBookSummaryDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(bookDTOs, pageable, books.size());
    }

    /**
     * Get statistics about categories.
     */
    public Map<String, Object> getCategoryStatistics() {
        // Update all category book counts to ensure accuracy
        updateAllCategoryBookCounts();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCategories", categoryRepository.count());
        stats.put("emptyCategories", categoryRepository.countByBookCount(0));
        stats.put("mostPopularCategory", categoryRepository.findTopByOrderByBookCountDesc()
                .map(Category::getName).orElse("None"));
        stats.put("avgBooksPerCategory", categoryRepository.getAverageBookCount());

        return stats;
    }

    /**
     * Merge two categories - improved to handle the many-to-many relationship properly
     */
    @Transactional
    public AdminCategoryDetailsDTO mergeCategories(CategoryMergeDTO mergeDTO) {
        Category sourceCategory = getCategoryById(mergeDTO.getSourceCategoryId());
        Category targetCategory = getCategoryById(mergeDTO.getTargetCategoryId());

        // Refresh book counts to ensure accuracy
        updateCategoryBookCount(sourceCategory.getId());
        updateCategoryBookCount(targetCategory.getId());

        // Reload entities after update
        sourceCategory = getCategoryById(mergeDTO.getSourceCategoryId());
        targetCategory = getCategoryById(mergeDTO.getTargetCategoryId());

        if (sourceCategory.getBookCount() == 0) {
            throw new IllegalStateException("Source category has no books to merge.");
        }

        // Use SQL to directly update the join table - most efficient approach
        int updatedRows = entityManager.createNativeQuery(
                        "INSERT INTO book_categories (book_id, category_id) " +
                                "SELECT bc.book_id, :targetCategoryId " +
                                "FROM book_category bc " +
                                "WHERE bc.category_id = :sourceCategoryId " +
                                "AND bc.book_id NOT IN (" +
                                "  SELECT book_id FROM book_category " +
                                "  WHERE category_id = :targetCategoryId" +
                                ")")
                .setParameter("sourceCategoryId", sourceCategory.getId())
                .setParameter("targetCategoryId", targetCategory.getId())
                .executeUpdate();

        // Remove source category associations
        if (mergeDTO.isDeleteSourceAfterMerge()) {
            entityManager.createNativeQuery(
                            "DELETE FROM book_categories WHERE category_id = :categoryId")
                    .setParameter("categoryId", sourceCategory.getId())
                    .executeUpdate();
        }

        // Update book counts
        updateCategoryBookCount(targetCategory.getId());
        updateCategoryBookCount(sourceCategory.getId());

        // Reload target category to get updated book count
        targetCategory = getCategoryById(targetCategory.getId());

        // Optionally delete source category if requested
        if (mergeDTO.isDeleteSourceAfterMerge()) {
            categoryRepository.delete(sourceCategory);
        }

        return convertToAdminCategoryDetailsDTO(targetCategory);
    }

    /**
     * Convert a Category entity to AdminCategoryDetailsDTO.
     */
    private AdminCategoryDetailsDTO convertToAdminCategoryDetailsDTO(Category category) {
        return AdminCategoryDetailsDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .bookCount(category.getBookCount())
                .build();
    }

    /**
     * Convert a Book entity to BookSummaryDTO.
     */
    private BookSummaryDTO convertToBookSummaryDTO(Book book) {
        return BookSummaryDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .build();
    }
}