package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Admin.BookManagement.BookSearchCriteria;
import com.BookBliss.DTO.Books.BookFilterDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Borrowing;
import com.BookBliss.Entity.Category;
import com.BookBliss.Entity.Reviews;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookSpecification {


    // specification based on availability
    public static Specification<Book> hasAvailability(Boolean available) {
        return (root, query, criteriaBuilder) -> {
            if (available == null) {
                return criteriaBuilder.conjunction();
            }
            return available ?
                    criteriaBuilder.greaterThan(root.get("availableCopies"), 0) :
                    criteriaBuilder.equal(root.get("availableCopies"), 0);
        };
    }

    // specification for publication year range
    public static Specification<Book> hasPublicationYearBetween(Integer yearFrom, Integer yearTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (yearFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("publicationYear"), yearFrom
                ));
            }

            if (yearTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("publicationYear"), yearTo
                ));
            }

            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // specification for categories as string
    public static Specification<Book> hasCategories(String categoriesStr) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(categoriesStr)) {
                return criteriaBuilder.conjunction();
            }

            List<String> categories = Arrays.asList(categoriesStr.split(","));
            if (categories.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            query.distinct(true); // Ensure distinct results when joining
            Join<Book, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return categoryJoin.get("name").in(categories);
        };
    }

    // specification for categories as list
    public static Specification<Book> hasCategories(List<String> categories) {
        return (root, query, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(categories)) {
                return criteriaBuilder.conjunction();
            }

            query.distinct(true); // Ensure distinct results when joining
            Join<Book, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return categoryJoin.get("name").in(categories);
        };
    }

    // specification for category by ID
    public static Specification<Book> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            Join<Book, Category> categoryJoin = root.join("categories", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    // specification for title search
    public static Specification<Book> titleContains(String title) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(title)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
        };
    }

    // specification for author search
    public static Specification<Book> authorContains(String author) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(author)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%"
            );
        };
    }

    // specification for ISBN search
    public static Specification<Book> isbnContains(String isbn) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(isbn)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("isbn")),
                    "%" + isbn.toLowerCase() + "%"
            );
        };
    }

    // specification for publisher search
    public static Specification<Book> publisherContains(String publisher) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(publisher)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("publisher")),
                    "%" + publisher.toLowerCase() + "%"
            );
        };
    }

    // specification for available copies range
    public static Specification<Book> hasAvailableCopiesBetween(Integer min, Integer max) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (min != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("availableCopies"), min
                ));
            }

            if (max != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("availableCopies"), max
                ));
            }

            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Method to check if available copies are less than or equal to threshold
    public static Specification<Book> hasAvailableCopiesLessThanOrEqual(Integer threshold) {
        return (root, query, criteriaBuilder) -> {
            if (threshold == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("availableCopies"), threshold);
        };
    }

    // specification for creation date range
    public static Specification<Book> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), from
                ));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), to
                ));
            }

            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // specification for update date range
    public static Specification<Book> updatedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("updatedAt"), from
                ));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("updatedAt"), to
                ));
            }

            return predicates.isEmpty() ?
                    criteriaBuilder.conjunction() :
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Method for average rating filter
    public static Specification<Book> hasAverageRatingGreaterThan(Double minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Double> reviewSubquery = query.subquery(Double.class);
            var reviewRoot = reviewSubquery.from(Reviews.class);
            reviewSubquery.select(criteriaBuilder.avg(reviewRoot.get("rating")))
                    .where(criteriaBuilder.equal(reviewRoot.get("book"), root));

            return criteriaBuilder.greaterThanOrEqualTo(
                    reviewSubquery,
                    minRating
            );
        };
    }

    // Method for borrow count filter
    public static Specification<Book> hasBorrowCountGreaterThan(Long minBorrowCount) {
        return (root, query, criteriaBuilder) -> {
            if (minBorrowCount == null) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> borrowSubquery = query.subquery(Long.class);
            var borrowRoot = borrowSubquery.from(Borrowing.class);
            borrowSubquery.select(criteriaBuilder.count(borrowRoot.get("id")))
                    .where(criteriaBuilder.equal(borrowRoot.get("book"), root));

            return criteriaBuilder.greaterThanOrEqualTo(
                    borrowSubquery,
                    minBorrowCount.longValue()
            );
        };
    }

    // specification for book status
    public static Specification<Book> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(status)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("status")),
                    status.toLowerCase()
            );
        };
    }

    // specification for keyword search across multiple fields
    public static Specification<Book> keywordSearch(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("publisher")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    // Composite methods that build specifications from DTOs

    // Build specification from BookFilterDTO
    public static Specification<Book> buildFromFilterDTO(BookFilterDTO filterDTO) {
        if (filterDTO == null) {
            return null;
        }

        Specification<Book> spec = Specification.where(null);

        // Add filters based on provided criteria
        if (filterDTO.getAvailable() != null) {
            spec = spec.and(hasAvailability(filterDTO.getAvailable()));
        }

        if (filterDTO.getYearFrom() != null || filterDTO.getYearTo() != null) {
            spec = spec.and(hasPublicationYearBetween(
                    filterDTO.getYearFrom(), filterDTO.getYearTo()));
        }

        if (filterDTO.getCategories() != null && !filterDTO.getCategories().isEmpty()) {
            spec = spec.and(hasCategories(filterDTO.getCategories()));
        }

        if (StringUtils.hasText(filterDTO.getTitle())) {
            spec = spec.and(titleContains(filterDTO.getTitle()));
        }

        if (StringUtils.hasText(filterDTO.getAuthor())) {
            spec = spec.and(authorContains(filterDTO.getAuthor()));
        }

        if (StringUtils.hasText(filterDTO.getIsbn())) {
            spec = spec.and(isbnContains(filterDTO.getIsbn()));
        }

        if (StringUtils.hasText(filterDTO.getPublisher())) {
            spec = spec.and(publisherContains(filterDTO.getPublisher()));
        }

        if(filterDTO.getAverageRatingGreaterThan() != null){
            spec = spec.and(hasAverageRatingGreaterThan(filterDTO.getAverageRatingGreaterThan()));
        }

        if (filterDTO.getAvailableCopiesMin() != null || filterDTO.getAvailableCopiesMax() != null) {
            spec = spec.and(hasAvailableCopiesBetween(
                    filterDTO.getAvailableCopiesMin(), filterDTO.getAvailableCopiesMax()));
        }

        if (filterDTO.getCreatedFrom() != null || filterDTO.getCreatedTo() != null) {
            spec = spec.and(createdBetween(
                    filterDTO.getCreatedFrom(), filterDTO.getCreatedTo()));
        }

        if (StringUtils.hasText(filterDTO.getKeyword())) {
            spec = spec.and(keywordSearch(filterDTO.getKeyword()));
        }

        return spec;
    }

    // Build specification from BookSearchCriteria
    public static Specification<Book> buildFromSearchCriteria(BookSearchCriteria criteria) {
        if (criteria == null) {
            return null;
        }

        Specification<Book> spec = Specification.where(null);

        // Handle keyword search
        if (StringUtils.hasText(criteria.getKeyword())) {
            spec = spec.and(keywordSearch(criteria.getKeyword()));
        }

        // Publisher filter
        if (StringUtils.hasText(criteria.getPublisher())) {
            spec = spec.and(publisherContains(criteria.getPublisher()));
        }

        // Publication year range
        if (criteria.getPublicationYearFrom() != null || criteria.getPublicationYearTo() != null) {
            spec = spec.and(hasPublicationYearBetween(
                    criteria.getPublicationYearFrom(), criteria.getPublicationYearTo()));
        }

        // Categories filter
        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            spec = spec.and(hasCategories(criteria.getCategories()));
        }

        // Availability filter
        if (criteria.getAvailable() != null) {
            spec = spec.and(hasAvailability(criteria.getAvailable()));
        }

        // Available copies range
        if (criteria.getAvailableCopiesLessThan() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("availableCopies"), criteria.getAvailableCopiesLessThan())
            );
        }

        if (criteria.getAvailableCopiesGreaterThan() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("availableCopies"), criteria.getAvailableCopiesGreaterThan())
            );
        }

        // Creation date range
        if (criteria.getCreatedAfter() != null || criteria.getCreatedBefore() != null) {
            spec = spec.and(createdBetween(criteria.getCreatedAfter(), criteria.getCreatedBefore()));
        }

        // Updated date range
        if (criteria.getUpdatedAfter() != null || criteria.getUpdatedBefore() != null) {
            spec = spec.and(updatedBetween(criteria.getUpdatedAfter(), criteria.getUpdatedBefore()));
        }

        // Average rating filter
        if (criteria.getAverageRatingGreaterThan() != null) {
            spec = spec.and(hasAverageRatingGreaterThan(criteria.getAverageRatingGreaterThan()));
        }

        // Borrow count filter
        if (criteria.getBorrowCountGreaterThan() != null) {
            spec = spec.and(hasBorrowCountGreaterThan(criteria.getBorrowCountGreaterThan()));
        }

        return spec;
    }
}