package com.BookBliss.Repository.ReadingSpace;

import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReadingProgressRepository extends JpaRepository<UserReadingProgress, Long> {
    List<UserReadingProgress> findByUserId(Long userId);
    List<UserReadingProgress> findByBookId(Long bookId);
    UserReadingProgress findByUserIdAndBookId(Long userId, Long bookId);

    List<UserReadingProgress> findByUserIdAndStatus(Long userId, UserReadingProgress.ReadingStatus status);
}
