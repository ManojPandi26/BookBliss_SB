package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingProgressDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.ReadingSpaceMapper;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.ReadingSpace.UserReadingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserReadingProgressServiceImpl implements UserReadingProgressService {

    private final UserReadingProgressRepository progressRepository;
    private final BookRepository bookRepository;
    private final ReadingSpaceMapper mapper;

    @Transactional
    @Override
    public UserReadingProgressDTO createProgress(UserReadingProgressDTO progressDTO) {
        UserReadingProgress progress = mapper.toURPEntity(progressDTO);
        progress.setStartedAt(LocalDateTime.now());
        progress.setStatus(UserReadingProgress.ReadingStatus.IN_PROGRESS);
        return mapper.toURPDTO(progressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserReadingProgressDTO> getProgressByUserAndStatus(Long userId, UserReadingProgress.ReadingStatus status) {
        return progressRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(mapper::toURPDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserReadingProgressDTO updateProgress(Long id, UserReadingProgressDTO progressDTO) {
        UserReadingProgress existingProgress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading Progress not found"));

        existingProgress.setCurrentPage(progressDTO.getCurrentPage());
        existingProgress.setReadingPercentage(progressDTO.getReadingPercentage());

        if (progressDTO.getStatus() == UserReadingProgress.ReadingStatus.COMPLETED) {
            existingProgress.setCompletedAt(LocalDateTime.now());
        }

        existingProgress.setStatus(progressDTO.getStatus());

        return mapper.toURPDTO(progressRepository.save(existingProgress));
    }

    @Transactional(readOnly = true)
    @Override
    public UserReadingProgressDTO getProgressByUserAndBook(Long userId, Long bookId) {
        UserReadingProgress progress = progressRepository.findByUserIdAndBookId(userId, bookId);
        if (progress == null) {
            throw new ResourceNotFoundException("No reading progress found for user and book");
        }
        return mapper.toURPDTO(progress);
    }

    // To implement this....
    @Transactional(readOnly = true)
    @Override
    public List<UserReadingProgressDTO> getUnreadBooks(Long userId) {
        // Find all books the user hasn't started reading
        List<Book> allBooks = bookRepository.findAll();

        List<Long> readBookIds = progressRepository.findByUserId(userId)
                .stream()
                .map(progress -> progress.getBook().getId())
                .toList();

        return allBooks.stream()
                .filter(book -> !readBookIds.contains(book.getId()))
                .map(book -> UserReadingProgressDTO.builder()
                        .userId(userId)
                        .bookId(book.getId())
                        .status(UserReadingProgress.ReadingStatus.NOT_STARTED)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserReadingProgressDTO> getUserProgressForBook(Long bookId) {
        // Find all user progresses for a specific book
        return progressRepository.findByBookId(bookId)
                .stream()
                .map(mapper::toURPDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserReadingProgressDTO> getUserAllProgresses(Long userId) {
        // Get all reading progresses for a user across all books
        return progressRepository.findByUserId(userId)
                .stream()
                .map(mapper::toURPDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserReadingProgressDTO startReading(Long userId, Long bookId) {
        // Check if progress already exists
        UserReadingProgress existingProgress = progressRepository
                .findByUserIdAndBookId(userId, bookId);

        if (existingProgress != null) {
            throw new IllegalStateException("Reading progress already exists for this book");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        User user = new User(); // Create an empty user
        user.setId(userId);

        UserReadingProgress newProgress = UserReadingProgress.builder()
                .user(user)
                .book(book)
                .currentPage(0)
                .readingPercentage(0.0)
                .status(UserReadingProgress.ReadingStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        return mapper.toURPDTO(progressRepository.save(newProgress));
    }

    @Transactional
    @Override
    public UserReadingProgressDTO abandonBook(Long progressId) {
        UserReadingProgress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));

        progress.setStatus(UserReadingProgress.ReadingStatus.ABANDONED);
        progress.setCompletedAt(LocalDateTime.now());

        return mapper.toURPDTO(progressRepository.save(progress));
    }
}
