package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingNoteDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserReadingNoteService {

    @Transactional
    UserReadingNoteDTO createNote(UserReadingNoteDTO noteDTO);

    @Transactional(readOnly = true)
    List<UserReadingNoteDTO> getNotesByUserAndBook(Long userId, Long bookId);

    @Transactional
    UserReadingNoteDTO updateNote(Long id, UserReadingNoteDTO noteDTO);

    @Transactional
    void deleteNote(Long id);
}
