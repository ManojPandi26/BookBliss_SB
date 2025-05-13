package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingNoteDTO;
import com.BookBliss.Entity.ReadinSpace.UserReadingNote;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.ReadingSpaceMapper;
import com.BookBliss.Repository.ReadingSpace.UserReadingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserReadingNoteServiceImpl implements UserReadingNoteService{

        private final UserReadingNoteRepository repository;
        private final ReadingSpaceMapper mapper;

        @Transactional
        @Override
        public UserReadingNoteDTO createNote(UserReadingNoteDTO noteDTO) {
            UserReadingNote note = mapper.toURNEntity(noteDTO);
            return mapper.toURNDTO(repository.save(note));
        }

        @Transactional(readOnly = true)
        @Override
        public List<UserReadingNoteDTO> getNotesByUserAndBook(Long userId, Long bookId) {
            return repository.findByUser_IdAndBook_Id(userId, bookId)
                    .stream()
                    .map(mapper::toURNDTO)
                    .collect(Collectors.toList());
        }

        @Transactional
        @Override
        public UserReadingNoteDTO updateNote(Long id, UserReadingNoteDTO noteDTO) {
            UserReadingNote existingNote = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

            existingNote.setPageNumber(noteDTO.getPageNumber());
            existingNote.setContent(noteDTO.getContent());

            return mapper.toURNDTO(repository.save(existingNote));
        }

        @Transactional
        @Override
        public void deleteNote(Long id) {
            UserReadingNote note = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
            repository.delete(note);
        }
}

