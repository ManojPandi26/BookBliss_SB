package com.BookBliss.Repository.ReadingSpace;

import com.BookBliss.Entity.ReadinSpace.UserReadingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserReadingNoteRepository extends JpaRepository<UserReadingNote, Long> {
    List<UserReadingNote> findByUser_IdAndBook_Id(Long userId, Long bookId);
}
