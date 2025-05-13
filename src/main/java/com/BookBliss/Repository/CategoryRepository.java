package com.BookBliss.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BookBliss.Entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByName(String name);
	
	@Query("SELECT c FROM Category c WHERE c.bookCount > :count")
    List<Category> findByBookCountGreaterThan(@Param("count") int count);

	long countByBookCount(int bookCount);

	Optional<Category> findTopByOrderByBookCountDesc();

	@Query("SELECT AVG(c.bookCount) FROM Category c")
	double getAverageBookCount();

	Page<Category> findAll(Specification<Category> spec, Pageable pageable);
}
