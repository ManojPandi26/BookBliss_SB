package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Admin.BookManagement.AdminBookDetailsDTO;
import com.BookBliss.DTO.Admin.BookManagement.BookSearchCriteria;
import com.BookBliss.DTO.Books.BookAddingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BookServiceAdmin {
    Page<AdminBookDetailsDTO> getAllBooksAdmin(Pageable pageable);

    AdminBookDetailsDTO getBookByIdAdmin(Long id);

    Page<AdminBookDetailsDTO> searchBooksAdmin(BookSearchCriteria criteria, Pageable pageable);

    @Transactional
    AdminBookDetailsDTO addBookAdmin(BookAddingDTO bookAddingDTO);

    @Transactional
    List<AdminBookDetailsDTO> addBooksAdmin(List<BookAddingDTO> bookAddingDTOs);

    @Transactional
    AdminBookDetailsDTO updateBookAdmin(Long id, BookAddingDTO updatedBookDTO);

    @Transactional
    void deleteBook(Long id);

    @Transactional
    AdminBookDetailsDTO incrementAvailableCopiesAdmin(Long bookId, int incrementBy);

    @Transactional
    AdminBookDetailsDTO decrementAvailableCopiesAdmin(Long bookId, int decrementBy);

    Page<AdminBookDetailsDTO> getBooksByCategoryAdmin(Long categoryId, Pageable pageable);

    Page<AdminBookDetailsDTO> getBooksWithLowAvailabilityAdmin(int threshold, Pageable pageable);
}
