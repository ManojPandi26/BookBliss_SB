package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.ReadingSessionDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReadingSessionService {
    @Transactional
    ReadingSessionDTO createSession(ReadingSessionDTO sessionDTO);

    @Transactional(readOnly = true)
    List<ReadingSessionDTO> getSessionsByUserReadingProgress(Long userReadingProgressId);

    @Transactional
    ReadingSessionDTO updateSession(Long id, ReadingSessionDTO sessionDTO);

    @Transactional
    void deleteSession(Long id);
}
