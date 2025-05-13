package com.BookBliss.Mapper;

import com.BookBliss.DTO.ReadingSpace.ReadingSessionDTO;
import com.BookBliss.DTO.ReadingSpace.UserReadingBookmarkDTO;
import com.BookBliss.DTO.ReadingSpace.UserReadingNoteDTO;
import com.BookBliss.DTO.ReadingSpace.UserReadingProgressDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.ReadinSpace.ReadingSession;
import com.BookBliss.Entity.ReadinSpace.UserReadingBookmark;
import com.BookBliss.Entity.ReadinSpace.UserReadingNote;
import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import com.BookBliss.Entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReadingSpaceMapper {

    public UserReadingProgressDTO toURPDTO(UserReadingProgress progress) {
        if (progress == null) return null;

        return UserReadingProgressDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser() != null ? progress.getUser().getId() : null)
                .bookId(progress.getBook() != null ? progress.getBook().getId() : null)
                .currentPage(progress.getCurrentPage())
                .readingPercentage(progress.getReadingPercentage())
                .status(progress.getStatus())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastReadAt(progress.getLastReadAt())
                .build();
    }

    public UserReadingProgress toURPEntity(UserReadingProgressDTO progressDTO) {
        if (progressDTO == null) return null;

        UserReadingProgress progress = new UserReadingProgress();

        if (progressDTO.getUserId() != null) {
            User user = new User();
            user.setId(progressDTO.getUserId());
            progress.setUser(user);
        }

        if (progressDTO.getBookId() != null) {
            Book book = new Book();
            book.setId(progressDTO.getBookId());
            progress.setBook(book);
        }

        progress.setId(progressDTO.getId());
        progress.setCurrentPage(progressDTO.getCurrentPage());
        progress.setReadingPercentage(progressDTO.getReadingPercentage());
        progress.setStatus(progressDTO.getStatus());
        progress.setStartedAt(progressDTO.getStartedAt());
        progress.setCompletedAt(progressDTO.getCompletedAt());
        progress.setLastReadAt(progressDTO.getLastReadAt());

        return progress;
    }

    // User Reading Bookmark

    public UserReadingBookmarkDTO toURBDTO(UserReadingBookmark bookmark) {
        if (bookmark == null) return null;

        return UserReadingBookmarkDTO.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUser() != null ? bookmark.getUser().getId() : null)
                .bookId(bookmark.getBook() != null ? bookmark.getBook().getId() : null)
                .pageNumber(bookmark.getPageNumber())
                .title(bookmark.getTitle())
                .description(bookmark.getDescription())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }

    public UserReadingBookmark toURBEntity(UserReadingBookmarkDTO bookmarkDTO) {
        if (bookmarkDTO == null) return null;

        UserReadingBookmark bookmark = new UserReadingBookmark();

        if (bookmarkDTO.getUserId() != null) {
            User user = new User();
            user.setId(bookmarkDTO.getUserId());
            bookmark.setUser(user);
        }

        if (bookmarkDTO.getBookId() != null) {
            Book book = new Book();
            book.setId(bookmarkDTO.getBookId());
            bookmark.setBook(book);
        }

        bookmark.setId(bookmarkDTO.getId());
        bookmark.setPageNumber(bookmarkDTO.getPageNumber());
        bookmark.setTitle(bookmarkDTO.getTitle());
        bookmark.setDescription(bookmarkDTO.getDescription());

        return bookmark;
    }

    // User Reading NOtes......

    public UserReadingNoteDTO toURNDTO(UserReadingNote note) {
        if (note == null) return null;

        return UserReadingNoteDTO.builder()
                .id(note.getId())
                .userId(note.getUser() != null ? note.getUser().getId() : null)
                .bookId(note.getBook() != null ? note.getBook().getId() : null)
                .pageNumber(note.getPageNumber())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    public UserReadingNote toURNEntity(UserReadingNoteDTO noteDTO) {
        if (noteDTO == null) return null;

        UserReadingNote note = new UserReadingNote();

        if (noteDTO.getUserId() != null) {
            User user = new User();
            user.setId(noteDTO.getUserId());
            note.setUser(user);
        }

        if (noteDTO.getBookId() != null) {
            Book book = new Book();
            book.setId(noteDTO.getBookId());
            note.setBook(book);
        }

        note.setId(noteDTO.getId());
        note.setPageNumber(noteDTO.getPageNumber());
        note.setContent(noteDTO.getContent());

        return note;
    }

    // Reading Sessions .........

    public ReadingSessionDTO toRSDTO(ReadingSession session) {
        if (session == null) return null;

        return ReadingSessionDTO.builder()
                .id(session.getId())
                .userReadingProgressId(session.getUserReadingProgress() != null
                        ? session.getUserReadingProgress().getId()
                        : null)
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .pagesRead(session.getPagesRead())
                .durationMinutes(session.getDurationMinutes())
                .sessionNotes(session.getSessionNotes())
                .build();
    }

    public ReadingSession toRSEntity(ReadingSessionDTO dto) {
        if (dto == null) return null;

        ReadingSession session = new ReadingSession();

        if (dto.getUserReadingProgressId() != null) {
            UserReadingProgress progress = new UserReadingProgress();
            progress.setId(dto.getUserReadingProgressId());
            session.setUserReadingProgress(progress);
        }

        session.setId(dto.getId());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setPagesRead(dto.getPagesRead());
        session.setDurationMinutes(dto.getDurationMinutes());
        session.setSessionNotes(dto.getSessionNotes());

        return session;
    }
}
