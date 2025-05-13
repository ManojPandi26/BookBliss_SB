package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.ReadingSessionDTO;
import com.BookBliss.Entity.ReadinSpace.ReadingSession;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.ReadingSpaceMapper;
import com.BookBliss.Repository.ReadingSpace.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingSessionServiceImpl implements ReadingSessionService{

    private final ReadingSessionRepository repository;
    private final ReadingSpaceMapper mapper;

    @Transactional
    @Override
    public ReadingSessionDTO createSession(ReadingSessionDTO sessionDTO) {
        ReadingSession session = mapper.toRSEntity(sessionDTO);
        return mapper.toRSDTO(repository.save(session));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReadingSessionDTO> getSessionsByUserReadingProgress(Long userReadingProgressId) {
        return repository.findByUserReadingProgress_Id(userReadingProgressId)
                .stream()
                .map(mapper::toRSDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ReadingSessionDTO updateSession(Long id, ReadingSessionDTO sessionDTO) {
        ReadingSession existingSession = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading Session not found"));

        existingSession.setEndTime(sessionDTO.getEndTime());
        existingSession.setPagesRead(sessionDTO.getPagesRead());
        existingSession.setDurationMinutes(sessionDTO.getDurationMinutes());
        existingSession.setSessionNotes(sessionDTO.getSessionNotes());

        return mapper.toRSDTO(repository.save(existingSession));
    }

    @Transactional
    @Override
    public void deleteSession(Long id) {
        ReadingSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading Session not found"));
        repository.delete(session);
    }
}
