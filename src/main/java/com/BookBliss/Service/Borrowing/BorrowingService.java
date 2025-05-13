package com.BookBliss.Service.Borrowing;

import com.BookBliss.DTO.Borrowing.BorrowingDTO;
import com.BookBliss.DTO.Borrowing.BorrowingStatusResponse;
import com.BookBliss.Entity.Borrowing;
import com.BookBliss.Entity.User;

import java.util.List;

public interface BorrowingService {

    // Existing methods
    BorrowingDTO createBorrowing(BorrowingDTO borrowingDTO);
    BorrowingDTO getBorrowingById(Long id);
    List<BorrowingDTO> getAllBorrowings(int page, int size);
    Long getBorrowingCountByBook(Long BookId);
    List<BorrowingDTO> getBorrowingsByUserId(Long userId);
    BorrowingDTO updateBorrowing(Long id, BorrowingDTO borrowingDTO);
    BorrowingDTO returnBorrowing(Long id);
    void deleteBorrowing(Long id);
    void validateUserBorrowingLimit(User user);
    void validateUserHasNoOverdue(User user);
    void handleBookBorrowing(Borrowing borrowing, Long bookId);
    void handleJournalBorrowing(Borrowing borrowing, Long journalId);
    void handleItemReturn(Borrowing borrowing);
    void calculateAndSetFine(Borrowing borrowing);
    void validatePaginationParameters(int page, int size);
    BorrowingStatusResponse checkBorrowingStatus(Long userId, Long bookId);


}