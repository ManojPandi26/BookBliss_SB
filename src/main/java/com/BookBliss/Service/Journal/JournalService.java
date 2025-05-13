package com.BookBliss.Service.Journal;

import com.BookBliss.DTO.JournalDTO;
import com.BookBliss.Entity.Journal;
import jakarta.transaction.Transactional;

import java.util.List;

public interface JournalService {

    JournalDTO createJournal(JournalDTO journalDTO);

    JournalDTO getJournalById(Long id);

    List<JournalDTO> getAllJournals(int page, int size, String publisher, String title);

    @Transactional
    JournalDTO updateJournal(Long id, JournalDTO journalDTO);

    @Transactional
    void deleteJournal(Long id);

    @Transactional
    JournalDTO updateAvailableCopies(Long id, int availableCopies);

    void validateJournalCopies(Journal journal);
}
