package com.BookBliss.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import java.util.Set;

@Entity
@Table(name = "books",indexes = {
        @Index(name = "idx_book_isbn", columnList = "isbn"),
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String author;

    @Size(max = 255, message = "Publisher name cannot exceed 255 characters")
    @Column(length = 255)
    private String publisher;

    @Column(name = "publication_year")
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2024, message = "Publication year cannot be in the future")
    private Integer publicationYear;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})([0-9]+[- ]){3}[0-9]+$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$", 
             message = "Invalid ISBN format")
    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Size(max = 50, message = "Edition cannot exceed 50 characters")
    @Column(length = 50)
    private String edition;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String coverImageUrl;

    @NotNull(message = "Available copies cannot be null")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(name = "available_copies")
    private Integer availableCopies;

    @NotNull(message = "Total copies cannot be null")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(name = "total_copies")
    private Integer totalCopies;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-Many relationship with Categories
    @JsonManagedReference
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    public void addCategory(Category category) {
        categories.add(category);
        category.getBooks().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getBooks().remove(this);
    }

    // Added for Reading space..........

//    @NotNull(message = "Total pages cannot be null")
//    @Min(value = 1, message = "Total pages must be at least 1")
//    @Column(name = "total_pages")
//    private Integer totalPages;
//
//    @Column(length = 255)
//    private String pdfFilePath;
//
//    // Reading Progress Relationship
//    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<UserReadingProgress> userProgresses = new HashSet<>();
//
//    // Bookmarks Relationship
//    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<UserReadingBookmark> userBookmarks = new HashSet<>();
//
//    // Reading Notes Relationship
//    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<UserReadingNote> userReadingNotes = new HashSet<>();


}