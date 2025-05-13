package com.BookBliss.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String searchTerm;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime searchedAt;

    @Column(nullable = false)
    private int searchCount;
}