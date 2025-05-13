package com.BookBliss.Repository;

import com.BookBliss.Entity.MyBookshelf;
import com.BookBliss.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyBookshelfRepository extends JpaRepository<MyBookshelf, Long> {

    List<MyBookshelf> findByUserOrderByCreatedAtDesc(User user);

    Optional<MyBookshelf> findByUserAndStatus(User user, MyBookshelf.BookshelfStatus status);

    @Query("SELECT b FROM MyBookshelf b WHERE b.user.id = :userId AND b.status = :status")
    Optional<MyBookshelf> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MyBookshelf.BookshelfStatus status);

    @Query("SELECT COUNT(b) FROM MyBookshelf b WHERE b.user.id = :userId AND b.status = :status")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MyBookshelf.BookshelfStatus status);

    List<MyBookshelf> findByStatus(MyBookshelf.BookshelfStatus status);

    @Query("SELECT b FROM MyBookshelf b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<MyBookshelf> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
