package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Admin.BookManagement.AdminBookDetailsDTO;
import com.BookBliss.DTO.Admin.BookManagement.BookSearchCriteria;
import com.BookBliss.DTO.Books.BookAddingDTO;
import com.BookBliss.DTO.Books.BookPreviewDTO;
import com.BookBliss.DTO.Books.BookSearchResponse;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Category;
import com.BookBliss.Exception.DuplicateResourceException;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.BookMapper;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.CategoryRepository;
import com.BookBliss.Repository.SearchHistoryRepository;
import com.BookBliss.Service.Borrowing.BorrowingServiceImpl;
import com.BookBliss.Service.Review.ReviewsServiceImpl;
import com.BookBliss.Service.Wishlist.WishlistServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookServiceAdminImpl implements BookServiceAdmin{

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private final Cache<Long, Book> bookCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .recordStats()
            .build();

    private final Cache<String, List<BookPreviewDTO>> recentBooksCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    private final Cache<String, List<BookSearchResponse>> searchCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private BorrowingServiceImpl borrowingService;

    @Autowired
    private ReviewsServiceImpl reviewsService;

    @Autowired
    private WishlistServiceImpl wishlistService;

    @Override
    public Page<AdminBookDetailsDTO> getAllBooksAdmin(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);


        // Map using the batch-fetched data
        return books.map(book -> {
            AdminBookDetailsDTO basicDto = bookMapper.toAdminBookDetailsDTO(book);
            // Enrich with pre-fetched data
            basicDto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
            basicDto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
            basicDto.setReviewCount(reviewsService.countReviewsByBook(book.getId()));
            return basicDto;
        });
    }

    @Override
    public AdminBookDetailsDTO getBookByIdAdmin(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
        AdminBookDetailsDTO dto=bookMapper.toAdminBookDetailsDTO(book);

        dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
        dto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
        dto.setReviewCount(reviewsService.countReviewsByBook(book.getId()));
        return dto;
    }

    @Override
    public Page<AdminBookDetailsDTO> searchBooksAdmin(BookSearchCriteria criteria, Pageable pageable) {
        Specification<Book> spec = BookSpecification.buildFromSearchCriteria(criteria);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(bookMapper::toAdminBookDetailsDTO);
    }

    @Transactional
    @Override
    public AdminBookDetailsDTO addBookAdmin(BookAddingDTO bookAddingDTO) {
        if(bookRepository.findByIsbn(bookAddingDTO.getIsbn())!=null){
            throw new DuplicateResourceException("A book with ISBN " + bookAddingDTO.getIsbn() + " already exists");
        }
        Book book = saveBookWithCategories(bookAddingDTO);
        bookCache.put(book.getId(), book);
        recentBooksCache.invalidateAll();
        return bookMapper.toAdminBookDetailsDTO(book);
    }

    @Transactional
    @Override
    public List<AdminBookDetailsDTO> addBooksAdmin(List<BookAddingDTO> bookAddingDTOs) {
        List<Book> savedBooks = bookAddingDTOs.stream()
                .map(this::saveBookWithCategories)
                .peek(book -> bookCache.put(book.getId(), book))
                .toList();

        recentBooksCache.invalidateAll();
        return savedBooks.stream()
                .map(bookMapper::toAdminBookDetailsDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AdminBookDetailsDTO updateBookAdmin(Long id, BookAddingDTO updatedBookDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        updateBookFields(existingBook, updatedBookDTO);
        Book savedBook = bookRepository.save(existingBook);
        bookCache.put(id, savedBook);
        return bookMapper.toAdminBookDetailsDTO(savedBook);
    }

    @Transactional
    @Override
    public void deleteBook(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            bookRepository.delete(book);
            bookCache.invalidate(id);
            recentBooksCache.invalidateAll();
        });
    }

//    @Transactional
//    public AdminBookDetailsDTO updateBookStatus(Long bookId, BookStatusUpdateDTO statusUpdateDTO) {
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
//
//        // Validate status - can add more statuses as needed
//        String status = statusUpdateDTO.getStatus().toUpperCase();
//        if (!List.of("ACTIVE", "UNAVAILABLE", "MAINTENANCE", "RESERVED").contains(status)) {
//            throw new InvalidOperationException("Invalid book status: " + status);
//        }
//
//        book.setStatus(status);
//        Book savedBook = bookRepository.save(book);
//        bookCache.put(bookId, savedBook);
//        return adminBookMapper.toAdminBookDetailsDTO(savedBook);
//    }

    @Transactional
    @Override
    public AdminBookDetailsDTO incrementAvailableCopiesAdmin(Long bookId, int incrementBy) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (incrementBy <= 0) {
            throw new InvalidOperationException("Increment value must be positive");
        }

        book.setAvailableCopies(book.getAvailableCopies() + incrementBy);
        book.setTotalCopies(book.getTotalCopies() + incrementBy);
        Book savedBook = bookRepository.save(book);
        bookCache.put(bookId, savedBook);

        return bookMapper.toAdminBookDetailsDTO(savedBook);
    }

    @Transactional
    @Override
    public AdminBookDetailsDTO decrementAvailableCopiesAdmin(Long bookId, int decrementBy) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (decrementBy <= 0) {
            throw new InvalidOperationException("Decrement value must be positive");
        }

        if (book.getAvailableCopies() < decrementBy) {
            throw new InvalidOperationException("Cannot decrement more copies than available");
        }

        book.setAvailableCopies(book.getAvailableCopies() - decrementBy);
        book.setTotalCopies(Math.max(book.getTotalCopies() - decrementBy, book.getAvailableCopies()));
        Book savedBook = bookRepository.save(book);
        bookCache.put(bookId, savedBook);

        return bookMapper.toAdminBookDetailsDTO(savedBook);
    }

    @Override
    public Page<AdminBookDetailsDTO> getBooksByCategoryAdmin(Long categoryId, Pageable pageable) {
        Specification<Book> spec = BookSpecification.hasCategoryId(categoryId);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(bookMapper::toAdminBookDetailsDTO);
    }

    @Override
    public Page<AdminBookDetailsDTO> getBooksWithLowAvailabilityAdmin(int threshold, Pageable pageable) {
        Specification<Book> spec = BookSpecification.hasAvailableCopiesLessThanOrEqual(threshold);
        Page<Book> books = bookRepository.findAll(spec, pageable);
        return books.map(bookMapper::toAdminBookDetailsDTO);
    }

//    @Transactional
//    public AdminBookDetailsDTO restoreBook(Long bookId) {
//        Book book = bookRepository.findBookWithDeleted(bookId)
//                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
//
//        if (book.getDeletedAt() == null) {
//            throw new InvalidOperationException("Book is not deleted");
//        }
//
//        book.setDeletedAt(null);
//        Book savedBook = bookRepository.save(book);
//        bookCache.put(bookId, savedBook);
//        return adminBookMapper.toAdminBookDetailsDTO(savedBook);
//    }

//    public Page<AdminBookDetailsDTO> getDeletedBooks(Pageable pageable) {
//        Page<Book> books = bookRepository.findByDeletedAtIsNotNull(pageable);
//        return books.map(adminBookMapper::toAdminBookDetailsDTO);
//    }

//    public Map<String, Object> getBookStatistics() {
//        Map<String, Object> statistics = new HashMap<>();
//
//        statistics.put("totalBooks", bookRepository.count());
//        statistics.put("totalAvailableBooks", bookRepository.countByAvailableCopiesGreaterThan(0));
//        statistics.put("totalUnavailableBooks", bookRepository.countByAvailableCopiesEquals(0));
//        statistics.put("averageBookRating", bookRepository.getAverageBookRating());
//        statistics.put("topCategories", bookRepository.getTopCategories(5));
//        statistics.put("booksAddedThisMonth", bookRepository.countByCreatedAtAfter(LocalDateTime.now().minusMonths(1)));
//        statistics.put("popularBooks", bookRepository.findMostBorrowedBooks(10));
//        statistics.put("recentlyAddedBooks", bookRepository.findTop5ByOrderByCreatedAtDesc());
//
//        return statistics;
//    }

    private Book saveBookWithCategories(BookAddingDTO dto) {
        Book book = bookMapper.convertToEntity(dto);
        Set<Category> categories = mapCategoryNamesToEntities(dto.getCategories());
        book.setCategories(categories);
        return bookRepository.save(book);
    }

    private Set<Category> mapCategoryNamesToEntities(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return new HashSet<>();
        }

        return categoryNames.stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(name);
                            return categoryRepository.save(newCategory);
                        }))
                .collect(Collectors.toSet());
    }

    private void updateBookFields(Book book, BookAddingDTO dto) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPublisher(dto.getPublisher());
        book.setPublicationYear(dto.getPublicationYear());
        book.setIsbn(dto.getIsbn());
        book.setEdition(dto.getEdition());
        book.setDescription(dto.getDescription());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setTotalCopies(dto.getTotalCopies());
        book.setCategories(mapCategoryNamesToEntities(dto.getCategories()));
    }

}
