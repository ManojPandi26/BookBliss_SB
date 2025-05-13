package com.BookBliss.Service.ReadingSpace;

import com.BookBliss.DTO.ReadingSpace.UserReadingBookmarkDTO;
import com.BookBliss.Entity.ReadinSpace.UserReadingBookmark;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.ReadingSpaceMapper;
import com.BookBliss.Repository.ReadingSpace.UserReadingBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserReadingBookmarkServiceImpl implements  UserReadingBookmarkService{
    private final UserReadingBookmarkRepository repository;
    private final ReadingSpaceMapper mapper;

    @Transactional
    @Override
    public UserReadingBookmarkDTO createBookmark(UserReadingBookmarkDTO bookmarkDTO) {
        UserReadingBookmark bookmark = mapper.toURBEntity(bookmarkDTO);
        return mapper.toURBDTO(repository.save(bookmark));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserReadingBookmarkDTO> getBookmarksByUserAndBook(Long userId, Long bookId) {
        return repository.findByUser_IdAndBook_Id(userId, bookId)
                .stream()
                .map(mapper::toURBDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserReadingBookmarkDTO updateBookmark(Long id, UserReadingBookmarkDTO bookmarkDTO) {
        UserReadingBookmark existingBookmark = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        existingBookmark.setPageNumber(bookmarkDTO.getPageNumber());
        existingBookmark.setTitle(bookmarkDTO.getTitle());
        existingBookmark.setDescription(bookmarkDTO.getDescription());

        return mapper.toURBDTO(repository.save(existingBookmark));
    }

    @Transactional
    @Override
    public void deleteBookmark(Long id) {
        UserReadingBookmark bookmark = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));
        repository.delete(bookmark);
    }
}
