package com.BookBliss.Service.Borrowing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.BookBliss.DTO.Borrowing.BorrowingStatusResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.BookBliss.DTO.Borrowing.BorrowingDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Borrowing;
import com.BookBliss.Entity.Journal;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Mapper.BorrowingMapper;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.BorrowingRepository;
import com.BookBliss.Repository.JournalRepository;
import com.BookBliss.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingServiceImpl implements BorrowingService{
    
    private static final int MAX_ACTIVE_BORROWINGS = 5;
    private static final int DEFAULT_BORROW_DAYS = 14;
    private static final BigDecimal FINE_RATE_PER_DAY = new BigDecimal("100.0");

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;
    private final BorrowingMapper borrowingMapper;

    @Override
    @Transactional
    public BorrowingDTO createBorrowing(BorrowingDTO borrowingDTO) {
        User user = userRepository.findById(borrowingDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + borrowingDTO.getUserId()));

        Book book = bookRepository.findById(borrowingDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + borrowingDTO.getBookId()));

        // Check if user already has an active borrowing for this book
        Optional<Borrowing> existingBorrowing = borrowingRepository.findByUserAndBookAndStatus(
                user,
                book,
                Borrowing.BorrowingStatus.BORROWED
        );

        if (existingBorrowing.isPresent()) {
            Borrowing existing = existingBorrowing.get();
            if (existing.getStatus() == Borrowing.BorrowingStatus.BORROWED) {
                throw new InvalidOperationException("You have already borrowed this book. You can start reading.");
            }
        }

        int availableCopies = book.getAvailableCopies();

        validateUserBorrowingLimit(user);
        validateUserHasNoOverdue(user);

        Borrowing borrowing = borrowingMapper.toEntity(borrowingDTO);
        borrowing.setUser(user);

        // Set default due date if not provided
        if (borrowing.getDueDate() == null) {
            borrowing.setDueDate(LocalDateTime.now().plusDays(DEFAULT_BORROW_DAYS));
        }

        if (borrowingDTO.getBookId() != null) {
            handleBookBorrowing(borrowing, borrowingDTO.getBookId());
        } else if (borrowingDTO.getJournalId() != null) {
            handleJournalBorrowing(borrowing, borrowingDTO.getJournalId());
        } else {
            throw new InvalidOperationException("Either bookId or journalId must be provided");
        }

        borrowing.setStatus(Borrowing.BorrowingStatus.BORROWED);
        book.setAvailableCopies(availableCopies - 1);

        Borrowing savedBorrowing = borrowingRepository.save(borrowing);
        log.info("Created new borrowing with ID: {} for user: {}", savedBorrowing.getId(), user.getUsername());

        return borrowingMapper.toDto(savedBorrowing);
    }

    @Override
    public BorrowingDTO getBorrowingById(Long id) {
        return borrowingMapper.toDto(borrowingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + id)));
    }

    @Override
    public List<BorrowingDTO> getAllBorrowings(int page, int size) {
        validatePaginationParameters(page, size);
        return borrowingRepository.findAll(PageRequest.of(page, size))
            .stream()
            .map(borrowingMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Long getBorrowingCountByBook(Long BookId){
        Book book = bookRepository.findById(BookId)
                .orElseThrow(()-> new ResourceNotFoundException("Book Not Found with the provided Id"));
        return borrowingRepository.countByBookId(book.getId());
    }

//    public String getBookStatus(Long bookId) {
//        Optional<Borrowing> latestBorrowing = borrowingRepository.findLatestBorrowingByBookId(bookId);
//
//        if (latestBorrowing.isEmpty()) {
//            return "AVAILABLE"; // No borrowing records, book is available
//        }
//        Borrowing borrowing = latestBorrowing.get();
//        // If the book has been returned, it's available
//        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
//            return "AVAILABLE";
//        }
//        // If the book is currently borrowed
//        if (borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED) {
//            // Check if it's overdue
//            if (LocalDateTime.now().isAfter(borrowing.getDueDate())) {
//                return "OVERDUE";
//            } else {
//                return "BORROWED";
//            }
//        }
//        // For OVERDUE status or any other status
//        return borrowing.getStatus().toString();
//    }

    @Override
    public List<BorrowingDTO> getBorrowingsByUserId(Long userId) {
     
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return borrowingRepository.findByUserId(userId)
            .stream()
            .map(borrowingMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BorrowingDTO updateBorrowing(Long id, BorrowingDTO borrowingDTO) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + id));
        
        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            throw new InvalidOperationException("Cannot update a returned borrowing");
        }

        borrowingMapper.updateEntityFromDto(borrowingDTO, borrowing);
        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Updated borrowing with ID: {}", id);
        return borrowingMapper.toDto(updatedBorrowing);
    }

    @Override
    @Transactional
    public BorrowingDTO returnBorrowing(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + id));

        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            throw new InvalidOperationException("Borrowing already returned");
        }

        handleItemReturn(borrowing);
        calculateAndSetFine(borrowing);

        borrowing.setStatus(Borrowing.BorrowingStatus.RETURNED);
        borrowing.setReturnDate(LocalDateTime.now());
        
        Borrowing returnedBorrowing = borrowingRepository.save(borrowing);
        log.info("Returned borrowing with ID: {}, fine amount: {}", id, borrowing.getFineAmount());
        return borrowingMapper.toDto(returnedBorrowing);
    }

    @Override
    @Transactional
    public void deleteBorrowing(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + id));
        
        if (borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED) {
            throw new InvalidOperationException("Cannot delete an active borrowing");
        }
        
        borrowingRepository.delete(borrowing);
        log.info("Deleted borrowing with ID: {}", id);
    }

    // to implement..............
    @Override
    public void validateUserBorrowingLimit(User user) {
        long activeBorrowings = borrowingRepository.countByUserIdAndStatus(
            user.getId(), Borrowing.BorrowingStatus.BORROWED);
       if (activeBorrowings >= MAX_ACTIVE_BORROWINGS) {
            throw new InvalidOperationException("User has reached maximum borrowing limit of " + MAX_ACTIVE_BORROWINGS);
        }
    }

    @Override
    public void validateUserHasNoOverdue(User user) {
        boolean hasOverdue = borrowingRepository.existsByUserIdAndStatus(
            user.getId(), Borrowing.BorrowingStatus.OVERDUE);
        if (hasOverdue) {
            throw new InvalidOperationException("User has overdue items and cannot borrow more");
        }
    }

    @Override
    public void handleBookBorrowing(Borrowing borrowing, Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        if (book.getAvailableCopies() < 1) {
            throw new InvalidOperationException("No copies available for borrowing");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        borrowing.setBook(book);
        bookRepository.save(book);
    }

    @Override
    public void handleJournalBorrowing(Borrowing borrowing, Long journalId) {
        Journal journal = journalRepository.findById(journalId)
            .orElseThrow(() -> new ResourceNotFoundException("Journal not found with ID: " + journalId));
        if (journal.getAvailableCopies() < 1) {
            throw new InvalidOperationException("No copies available for borrowing");
        }
        journal.setAvailableCopies(journal.getAvailableCopies() - 1);
        borrowing.setJournal(journal);
        journalRepository.save(journal);
    }

    @Override
    public void handleItemReturn(Borrowing borrowing) {
        if (borrowing.getBook() != null) {
            Book book = borrowing.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        } else if (borrowing.getJournal() != null) {
            Journal journal = borrowing.getJournal();
            journal.setAvailableCopies(journal.getAvailableCopies() + 1);
            journalRepository.save(journal);
        }
    }

    @Override
    public void calculateAndSetFine(Borrowing borrowing) {
    	if (LocalDateTime.now().isAfter(borrowing.getDueDate())) { 
    		long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
    		BigDecimal daysOverdueBigDecimal = BigDecimal.valueOf(daysOverdue); 
    		BigDecimal fineAmount = daysOverdueBigDecimal.multiply(FINE_RATE_PER_DAY); 
    		borrowing.setFineAmount(fineAmount); 
    		if (borrowing.getStatus() != Borrowing.BorrowingStatus.OVERDUE) { 
    			borrowing.setStatus(Borrowing.BorrowingStatus.OVERDUE); 
    		} 
    	} 
    }

    @Override
    public void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new InvalidOperationException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new InvalidOperationException("Page size must be greater than zero");
        }
    }

    @Override
    public BorrowingStatusResponse checkBorrowingStatus(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        Optional<Borrowing> existingBorrowing = borrowingRepository
                .findByUserAndBookAndStatus(user, book, Borrowing.BorrowingStatus.BORROWED);

        if (existingBorrowing.isPresent()) {
            Borrowing borrowing = existingBorrowing.get();
            return BorrowingStatusResponse.builder()
                    .borrowed(true)
                    .dueDate(borrowing.getDueDate())
                    .borrowDate(existingBorrowing.get().getBorrowDate())
                    .message("You have already borrowed this book. You can start reading.")
                    .build();
        }

        return BorrowingStatusResponse.builder()
                .borrowed(false)
                .message("Book is available for borrowing.")
                .build();
    }
}