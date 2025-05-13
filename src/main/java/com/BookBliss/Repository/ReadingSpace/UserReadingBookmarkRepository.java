package com.BookBliss.Repository.ReadingSpace;

import com.BookBliss.Entity.ReadinSpace.UserReadingBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReadingBookmarkRepository extends JpaRepository<UserReadingBookmark, Long> {
    List<UserReadingBookmark> findByUser_IdAndBook_Id(Long userId, Long bookId);
}
