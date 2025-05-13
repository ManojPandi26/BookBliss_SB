package com.BookBliss.Repository;

import com.BookBliss.Entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findTop10ByUserIdOrderBySearchedAtDesc(Long userId);
    List<SearchHistory> findTop10ByOrderBySearchCountDesc();
    Optional<SearchHistory> findByUserIdAndSearchTerm(Long userId, String searchTerm);
}
