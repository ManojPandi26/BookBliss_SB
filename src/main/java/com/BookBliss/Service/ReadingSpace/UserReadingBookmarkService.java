package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingBookmarkDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserReadingBookmarkService {
    @Transactional
    UserReadingBookmarkDTO createBookmark(UserReadingBookmarkDTO bookmarkDTO);

    @Transactional(readOnly = true)
    List<UserReadingBookmarkDTO> getBookmarksByUserAndBook(Long userId, Long bookId);

    @Transactional
    UserReadingBookmarkDTO updateBookmark(Long id, UserReadingBookmarkDTO bookmarkDTO);

    @Transactional
    void deleteBookmark(Long id);
}
