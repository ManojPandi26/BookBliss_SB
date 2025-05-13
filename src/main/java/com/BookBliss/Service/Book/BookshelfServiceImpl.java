package com.BookBliss.Service.Book;

import com.BookBliss.DTO.Books.BookShelfDTOs.BookshelfItemRequest;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.BookshelfItem;
import com.BookBliss.Entity.MyBookshelf;
import com.BookBliss.Entity.User;

import com.BookBliss.Exception.BookshelfException;
import com.BookBliss.Exception.BookshelfItemException;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Exception.UserNotFoundException;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.BookshelfItemRepository;
import com.BookBliss.Repository.MyBookshelfRepository;
import com.BookBliss.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl {

    private final MyBookshelfRepository bookshelfRepository;
    private final BookshelfItemRepository bookshelfItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * Gets the active bookshelf for a user, creating one if it doesn't exist
     */
    @Transactional
    public MyBookshelf getOrCreateActiveBookshelf(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Optional<MyBookshelf> existingBookshelf = bookshelfRepository.findByUserAndStatus(user, MyBookshelf.BookshelfStatus.ACTIVE);

        if (existingBookshelf.isPresent()) {
            return existingBookshelf.get();
        }

        MyBookshelf newBookshelf = new MyBookshelf();
        newBookshelf.setUser(user);
        newBookshelf.setStatus(MyBookshelf.BookshelfStatus.ACTIVE);

        return bookshelfRepository.save(newBookshelf);
    }

    /**
     * Get all bookshelves for a user
     */
    public List<MyBookshelf> getUserBookshelves(Long userId) {
        return bookshelfRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get a specific bookshelf by ID
     */
    public MyBookshelf getBookshelfById(Long bookshelfId) {
        return bookshelfRepository.findById(bookshelfId)
                .orElseThrow(() -> new BookshelfException("BookShelf Not Found with id"+bookshelfId));
    }

    public Boolean isInBookshelf(Long userId, Long bookId) {
        MyBookshelf activeBookshelf = bookshelfRepository.findByUserIdAndStatus(userId, MyBookshelf.BookshelfStatus.ACTIVE)
                .orElse(null);

        if (activeBookshelf == null) {
            return false;
        }

        return bookshelfItemRepository.findByBookshelfIdAndBookId(activeBookshelf.getId(), bookId).isPresent();
    }

    /**
     * Add a book to the active bookshelf
     */
    @Transactional
    public BookshelfItem addBookToBookshelf(Long userId, BookshelfItemRequest request) {
        MyBookshelf bookshelf = getOrCreateActiveBookshelf(userId);

        if (bookshelf.getStatus() != MyBookshelf.BookshelfStatus.ACTIVE) {
            throw new BookshelfException("Cannot add books to a bookshelf that is not active");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        // Check if book is available
        if (book.getAvailableCopies() < request.getQuantity()) {
            throw new ResourceNotFoundException("Not enough copies available. Requested: " +
                    request.getQuantity() + ", Available: " + book.getAvailableCopies());
        }

        // Check if the book is already in the bookshelf
        Optional<BookshelfItem> existingItem = bookshelfItemRepository.findByBookshelfIdAndBookId(
                bookshelf.getId(), book.getId());

        BookshelfItem bookshelfItem;

        if (existingItem.isPresent()) {
            // Update existing item quantity
            bookshelfItem = existingItem.get();
            int newQuantity = bookshelfItem.getQuantity() + request.getQuantity();

            if (book.getAvailableCopies() < newQuantity) {
                throw new ResourceNotFoundException("Not enough copies available for the total requested. " +
                        "Current in cart: " + bookshelfItem.getQuantity() +
                        ", Additional requested: " + request.getQuantity() +
                        ", Available: " + book.getAvailableCopies());
            }

            bookshelfItem.setQuantity(newQuantity);
        } else {
            // Create new bookshelf item
            bookshelfItem = new BookshelfItem();
            bookshelfItem.setBookshelf(bookshelf);
            bookshelfItem.setBook(book);
            bookshelfItem.setQuantity(request.getQuantity());
        }

        return bookshelfItemRepository.save(bookshelfItem);
    }

    /**
     * Update the quantity of a book in the bookshelf
     */
    @Transactional
    public BookshelfItem updateBookshelfItemQuantity(Long bookshelfId, Long bookId, int quantity) {
        MyBookshelf bookshelf = getBookshelfById(bookshelfId);

        if (bookshelf.getStatus() != MyBookshelf.BookshelfStatus.ACTIVE) {
            throw new BookshelfException("Cannot update items in a bookshelf that is not active");
        }

        BookshelfItem item = bookshelfItemRepository.findByBookshelfIdAndBookId(bookshelfId, bookId)
                .orElseThrow(() -> new BookshelfItemException("Book with id " + bookId + " not found in bookshelf with id " + bookshelfId));

        Book book = item.getBook();

        // Check availability if increasing quantity
        if (quantity > item.getQuantity()) {
            int additionalNeeded = quantity - item.getQuantity();
            if (book.getAvailableCopies() < additionalNeeded) {
                throw new ResourceNotFoundException("Not enough copies available. Additional needed: " +
                        additionalNeeded + ", Available: " + book.getAvailableCopies());
            }
        }

        item.setQuantity(quantity);
        return bookshelfItemRepository.save(item);
    }

    /**
     * Remove a book from the bookshelf
     */
    @Transactional
    public void removeBookFromBookshelf(Long bookshelfId, Long bookId) {
        MyBookshelf bookshelf = getBookshelfById(bookshelfId);

        if (bookshelf.getStatus() != MyBookshelf.BookshelfStatus.ACTIVE) {
            throw new BookshelfException("Cannot remove items from a bookshelf that is not active");
        }

        // Check if the book exists in the bookshelf
        BookshelfItem item = bookshelfItemRepository.findByBookshelfIdAndBookId(bookshelfId, bookId)
                .orElseThrow(() -> new BookshelfItemException("Book with id " + bookId + " not found in bookshelf with id " + bookshelfId));

        bookshelfItemRepository.delete(item);
    }

    /**
     * Clear all items from the bookshelf
     */
    @Transactional
    public void clearBookshelf(Long bookshelfId) {
        MyBookshelf bookshelf = getBookshelfById(bookshelfId);

        if (bookshelf.getStatus() != MyBookshelf.BookshelfStatus.ACTIVE) {
            throw new BookshelfException("Cannot clear a bookshelf that is not active");
        }

        bookshelf.getBookshelfItems().clear();
        bookshelfRepository.save(bookshelf);
    }

    /**
     * Update bookshelf status
     */
    @Transactional
    public MyBookshelf updateBookshelfStatus(Long bookshelfId, MyBookshelf.BookshelfStatus status) {
        MyBookshelf bookshelf = getBookshelfById(bookshelfId);
        bookshelf.setStatus(status);
        return bookshelfRepository.save(bookshelf);
    }

    /**
     * Get count of active bookshelf items for a user
     */
    public int getActiveBookshelfItemCount(Long userId) {
        Optional<MyBookshelf> activeBookshelf = bookshelfRepository.findByUserIdAndStatus(
                userId, MyBookshelf.BookshelfStatus.ACTIVE);

        if (!activeBookshelf.isPresent()) {
            return 0;
        }

        return activeBookshelf.get().getTotalItems();
    }
}
