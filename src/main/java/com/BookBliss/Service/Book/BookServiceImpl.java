package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Books.*;
import com.BookBliss.Entity.*;
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
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
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
    public BookDetailsDTO getBookById(Long id) {
        Book book = bookCache.get(id, key -> bookRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id)));
        BookDetailsDTO dto= bookMapper.convertToDetailsDTO(book);
            dto.setAverageRatingGreaterThan(reviewsService.getAverageBookRating(book.getId()));
            dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
            dto.setIsNew(this.isBookNew(book.getId()));
            dto.setRatingCount(reviewsService.countReviewsByBook(book.getId()));
            dto.setWishlistedCount(wishlistService.getBookWishlistCount(book.getId()));
        return dto;
    }

    /**
     * Checks if a book is new (added within the last week)
     *
     * @param bookId the ID of the book to check
     * @return true if the book was added within the last week, false otherwise
     */
    public boolean isBookNew(Long bookId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return bookRepository.isBookNew(bookId, oneWeekAgo);
    }


    @Override
    public List<BookSearchResponse> searchBooks(String query, int limit) {
        String cacheKey = query.toLowerCase() + "_" + limit;
        return searchCache.get(cacheKey, k -> bookRepository.searchBooks(query.toLowerCase(), limit)
                .stream()
                .map(book -> new BookSearchResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn()

                ))
                .collect(Collectors.toList()));
    }

    @Override
    public List<BookSummaryDTO> getRecentBooksPreview() {
        List<Book> books =  bookRepository.findTop8ByOrderByCreatedAtDesc();
         return books.stream().map(book ->{
            BookSummaryDTO dto= bookMapper.convertToSummaryDTO(book);
            dto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
            dto.setIsNew(this.isBookNew(book.getId()));
            dto.setRatingCount(reviewsService.countReviewsByBook(book.getId()));
            dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
            return dto;}).toList();

    }

    // Deprecated ......
    @Override
    public Page<Book> findBooks(Boolean available, Integer yearFrom, Integer yearTo, String categories, Pageable pageable) {
        List<String> categoryList = categories != null ? Arrays.asList(categories.split(",")) : null;
        return bookRepository.findBooksWithFilters(available, yearFrom, yearTo, categoryList, pageable);
    }

    @Override
    public List<BookSummaryDTO> getSimilarBooks(Long bookId, int limit) {
        Book originalBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        Set<Category> bookCategories = originalBook.getCategories();
        String author = originalBook.getAuthor();

        List<Book> similarBooks = bookRepository.findSimilarBooks(
                bookId,
                author,
                bookCategories.stream().map(Category::getName).collect(Collectors.toList()),
                PageRequest.of(0, limit)
        );

        return similarBooks.stream()
                .map(this::cacheAndConvertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookSummaryDTO> getBooksByAuthor(String authorName, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(authorName, pageable)
                .map(this::cacheAndConvertToDTO);
    }


    @Override
    public List<Book> getRecentlyAddedBooks() {
        return bookRepository.findTop5ByOrderByCreatedAtDesc();
    }


    private BookSummaryDTO cacheAndConvertToDTO(Book book) {
        bookCache.put(book.getId(), book);
        BookSummaryDTO dto= bookMapper.convertToSummaryDTO(book);
        dto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
        dto.setIsNew(this.isBookNew(book.getId()));
        dto.setRatingCount(reviewsService.countReviewsByBook(book.getId()));
        dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
        return dto;
    }

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

    @PostConstruct
    private void logCacheStats() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CacheStats stats = bookCache.stats();
                log.info("Book Cache Stats - Hit rate: {}%, Hit count: {}, Miss count: {}, Load count: {}, Eviction count: {}",
                        String.format("%.2f", stats.hitRate() * 100),
                        stats.hitCount(),
                        stats.missCount(),
                        stats.loadCount(),
                        stats.evictionCount()
                );
            }
        }, 0, TimeUnit.MINUTES.toMillis(5));
    }

    // feb 6
    public DynamicSearchResponse dynamicSearch(String query, Long userId, int limit) {
        if (StringUtils.isBlank(query)) {
            throw new InvalidOperationException("Search query cannot be empty");
        }

        // Sanitize and normalize the query
        String sanitizedQuery = sanitizeSearchQuery(query);

        // Get exact matches
        List<BookSearchResponse> exactMatches = searchBooks(sanitizedQuery, limit);

        // Get suggested matches using fuzzy search
        List<BookSearchResponse> suggestedMatches = searchBooksWithFuzzyMatch(sanitizedQuery, limit);

        // Get trending searches (always available)
        List<String> trendingSearches = getTrendingSearches();

        // Get user's recent searches if userId is available
        List<String> recentSearches = userId != null ?
                getRecentSearches(userId) :
                Collections.emptyList();

        // Update search history if userId is available
        if (userId != null) {
            try {
                updateSearchHistory(sanitizedQuery, userId);
            } catch (Exception e) {
                log.warn("Failed to update search history for user {}", userId, e);
                // Continue processing - don't fail the search if history update fails
            }
        }

        return new DynamicSearchResponse(
                exactMatches,
                suggestedMatches,
                trendingSearches,
                recentSearches
        );
    }

    private String sanitizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        // Remove any potentially harmful characters and normalize whitespace
        return query.trim()
                .replaceAll("[<>%$]", "")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private List<BookSearchResponse> searchBooksWithFuzzyMatch(String query, int limit) {
        String cacheKey = "fuzzy_" + query.toLowerCase() + "_" + limit;
        return searchCache.get(cacheKey, k -> bookRepository.searchBooksWithFuzzyMatch(query.toLowerCase(), limit)
                .stream()
                .map(book -> new BookSearchResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn()
                ))
                .collect(Collectors.toList()));
    }

    private void updateSearchHistory(String searchTerm, Long userId) {
        if (userId == null || StringUtils.isBlank(searchTerm)) {
            return;
        }

        try {
            SearchHistory searchHistory = searchHistoryRepository
                    .findByUserIdAndSearchTerm(userId, searchTerm.toLowerCase())
                    .orElse(SearchHistory.builder()
                            .userId(userId)
                            .searchTerm(searchTerm.toLowerCase())
                            .searchCount(0)
                            .build());

            searchHistory.setSearchCount(searchHistory.getSearchCount() + 1);
            searchHistory.setSearchedAt(LocalDateTime.now());
            searchHistoryRepository.save(searchHistory);
        } catch (Exception e) {
            log.error("Error updating search history for user {} and term {}", userId, searchTerm, e);
            throw new InvalidOperationException("Failed to update search history");
        }
    }

    public List<String> getTrendingSearches() {
        return searchHistoryRepository
                .findTop10ByOrderBySearchCountDesc()
                .stream()
                .map(SearchHistory::getSearchTerm)
                .collect(Collectors.toList());
    }

    public List<String> getRecentSearches(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            return searchHistoryRepository
                    .findTop10ByUserIdOrderBySearchedAtDesc(userId)
                    .stream()
                    .map(SearchHistory::getSearchTerm)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent searches for user {}", userId, e);
            return Collections.emptyList();
        }
    }


    @Override
    public Page<BookSummaryDTO> findBooksByFilter(BookFilterDTO filterDTO, Pageable pageable) {
        Specification<Book> spec = BookSpecification.buildFromFilterDTO(filterDTO);
        Page<Book> Books= bookRepository.findAll(spec, pageable);
        return Books.map(book -> {
            BookSummaryDTO dto= bookMapper.convertToSummaryDTO(book);
            dto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
            dto.setIsNew(this.isBookNew(book.getId()));
            dto.setRatingCount(reviewsService.countReviewsByBook(book.getId()));
            dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
            return dto;
        });
    }

    @Override
    public Page<BookSummaryDTO> searchBooksCriteria(String keyword, Pageable pageable) {
        Specification<Book> spec = BookSpecification.keywordSearch(keyword);
        Page<Book> Books= bookRepository.findAll(spec, pageable);
        return Books.map(book ->{
            BookSummaryDTO dto= bookMapper.convertToSummaryDTO(book);
            dto.setAverageRating(reviewsService.getAverageBookRating(book.getId()));
            dto.setIsNew(this.isBookNew(book.getId()));
            dto.setRatingCount(reviewsService.countReviewsByBook(book.getId()));
            dto.setBorrowCount(borrowingService.getBorrowingCountByBook(book.getId()));
            return dto;
        });
    }


    // ----------- ********************************** ADMIN **************************************--------------
    // Admin book operations



}