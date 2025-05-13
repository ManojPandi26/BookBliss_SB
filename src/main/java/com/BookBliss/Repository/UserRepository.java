package com.BookBliss.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BookBliss.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime date);

    Page<User> findAll(Specification<User> spec, Pageable pageable);


    // march 7
    @Query("SELECT u FROM User u WHERE u.LastLogin < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRole(@Param("role") User.UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByKeyword(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (u.role = :role) AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchByRoleAndKeyword(@Param("role") User.UserRole role, @Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.emailVerified = :verified")
    Page<User> findByEmailVerified(@Param("verified") boolean verified, Pageable pageable);
}
