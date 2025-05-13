package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingProgressDTO;
import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserReadingProgressService {
    @Transactional
    UserReadingProgressDTO createProgress(UserReadingProgressDTO progressDTO);

    @Transactional(readOnly = true)
    List<UserReadingProgressDTO> getProgressByUserAndStatus(Long userId, UserReadingProgress.ReadingStatus status);

    @Transactional
    UserReadingProgressDTO updateProgress(Long id, UserReadingProgressDTO progressDTO);

    @Transactional(readOnly = true)
    UserReadingProgressDTO getProgressByUserAndBook(Long userId, Long bookId);

    @Transactional(readOnly = true)
    List<UserReadingProgressDTO> getUnreadBooks(Long userId);

    @Transactional(readOnly = true)
    List<UserReadingProgressDTO> getUserProgressForBook(Long bookId);

    @Transactional(readOnly = true)
    List<UserReadingProgressDTO> getUserAllProgresses(Long userId);

    @Transactional
    UserReadingProgressDTO startReading(Long userId, Long bookId);

    @Transactional
    UserReadingProgressDTO abandonBook(Long progressId);
}
