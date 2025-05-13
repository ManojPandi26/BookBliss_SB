package com.BookBliss.Service.Journal;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.BookBliss.DTO.JournalDTO;
import com.BookBliss.Entity.Journal;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.JournalMapper;
import com.BookBliss.Repository.BorrowingRepository;
import com.BookBliss.Repository.JournalRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {
	
	    private final JournalRepository journalRepository;
	    private final BorrowingRepository borrowingRepository;
	    private final JournalMapper journalMapper;

		@Override
	    @Transactional
	    public JournalDTO createJournal(JournalDTO journalDTO) {
	        Journal journal = journalMapper.toEntity(journalDTO);
	        validateJournalCopies(journal);
	        return journalMapper.toDto(journalRepository.save(journal));
	    }

	    @Override
	    public JournalDTO getJournalById(Long id) {
	        return journalMapper.toDto(journalRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Journal not found")));
	    }

	    @Override
		public List<JournalDTO> getAllJournals(int page, int size, String publisher, String title) {
	        return journalRepository.findByPublisherAndTitleContaining(publisher, title, PageRequest.of(page, size))
	            .stream()
	            .map(journalMapper::toDto)
	            .collect(Collectors.toList());
	    }

	    @Transactional
		@Override
		public JournalDTO updateJournal(Long id, JournalDTO journalDTO) {
	        Journal journal = journalRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Journal not found"));
	        
	        journalMapper.updateEntityFromDto(journalDTO, journal);
	        validateJournalCopies(journal);
	        return journalMapper.toDto(journalRepository.save(journal));
	    }

		@Transactional
		@Override
		public void deleteJournal(Long id) {
	        Journal journal = journalRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Journal not found"));
	            
	        // Check if journal has any active borrowings
	        if (borrowingRepository.existsByJournalIdAndStatus(id, com.BookBliss.Entity.Borrowing.BorrowingStatus.BORROWED)) {
	            throw new InvalidOperationException("Cannot delete journal with active borrowings");
	        }
	        
	        journalRepository.delete(journal);
	    }

	    @Transactional
		@Override
		public JournalDTO updateAvailableCopies(Long id, int availableCopies) {
	        Journal journal = journalRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Journal not found"));
	            
	        if (availableCopies > journal.getTotalCopies()) {
	            throw new InvalidOperationException("Available copies cannot exceed total copies");
	        }
	        
	        if (availableCopies < 0) {
	            throw new InvalidOperationException("Available copies cannot be negative");
	        }
	        
	        journal.setAvailableCopies(availableCopies);
	        return journalMapper.toDto(journalRepository.save(journal));
	    }

		@Override
		public void validateJournalCopies(Journal journal) {
	        if (journal.getAvailableCopies() > journal.getTotalCopies()) {
	            throw new InvalidOperationException("Available copies cannot exceed total copies");
	        }
	        if (journal.getAvailableCopies() < 0 || journal.getTotalCopies() < 1) {
	            throw new InvalidOperationException("Invalid number of copies");
	        }
	    }

}
