package com.BookBliss.Repository.ReadingSpace;

import com.BookBliss.Entity.ReadinSpace.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByUserReadingProgress_Id(Long userReadingProgressId);
}
