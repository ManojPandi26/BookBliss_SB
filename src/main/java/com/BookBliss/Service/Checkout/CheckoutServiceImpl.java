package com.BookBliss.Service.Checkout;


import com.BookBliss.DTO.CheckOut.CheckoutDTOs.CheckoutRequest;
import com.BookBliss.DTO.CheckOut.CheckoutDTOs.CheckoutStatusUpdateRequest;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.BookshelfItem;
import com.BookBliss.Entity.Checkout;
import com.BookBliss.Entity.MyBookshelf;
import com.BookBliss.Entity.User;

import com.BookBliss.Exception.*;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.CheckoutRepository;
import com.BookBliss.Repository.MyBookshelfRepository;
import com.BookBliss.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl {

    private final CheckoutRepository checkoutRepository;
    private final MyBookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Initiate checkout process for a bookshelf
     */
    @Transactional
    public Checkout initiateCheckout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        MyBookshelf bookshelf = bookshelfRepository.findById(request.getBookshelfId())
                .orElseThrow(() -> new BookshelfException("BookShelf Not Found with id"+request.getBookshelfId()));

        // Validate the bookshelf belongs to the user
        if (!bookshelf.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Bookshelf does not belong to the user");
        }

        // Validate bookshelf status
        if (bookshelf.getStatus() != MyBookshelf.BookshelfStatus.ACTIVE) {
            throw new BookshelfException("Only active bookshelves can be checked out");
        }

        // Validate bookshelf has items
        if (bookshelf.getBookshelfItems().isEmpty()) {
            throw new BookshelfException("Cannot checkout an empty bookshelf");
        }

        // Verify all books are available in the requested quantities
        for (BookshelfItem item : bookshelf.getBookshelfItems()) {
            Book book = item.getBook();
            if (book.getAvailableCopies() < item.getQuantity()) {
                throw new ResourceNotFoundException("Book '" + book.getTitle() +
                        "' doesn't have enough available copies. Requested: " +
                        item.getQuantity() + ", Available: " + book.getAvailableCopies());
            }
        }

        // Update bookshelf status
        bookshelf.setStatus(MyBookshelf.BookshelfStatus.CHECKOUT);
        bookshelfRepository.save(bookshelf);

        // Generate checkout code
        String checkoutCode = generateCheckoutCode();

        // Calculate due date
        LocalDate dueDate = LocalDate.now().plusDays(request.getBorrowingDays());

        // Create checkout record
        Checkout checkout = new Checkout();
        checkout.setUser(user);
        checkout.setBookshelf(bookshelf);
        checkout.setCheckoutCode(checkoutCode);
        checkout.setBorrowingDays(request.getBorrowingDays());
        checkout.setDueDate(dueDate);
        checkout.setStatus(Checkout.CheckoutStatus.PENDING);
        checkout.setAdditionalNotes(request.getAdditionalNotes());

        return checkoutRepository.save(checkout);
    }

    /**
     * Confirm checkout (books are borrowed and removed from inventory)
     */
    @Transactional
    public Checkout confirmCheckout(Long checkoutId) {
        Checkout checkout = getCheckoutById(checkoutId);

        // Validate checkout status
        if (checkout.getStatus() != Checkout.CheckoutStatus.PENDING) {
            throw new InvalidOperationException(
                    "Checkout must be in PENDING status to be confirmed");
        }

        MyBookshelf bookshelf = checkout.getBookshelf();

        // Update book inventory
        for (BookshelfItem item : bookshelf.getBookshelfItems()) {
            Book book = item.getBook();

            // Verify availability again
            if (book.getAvailableCopies() < item.getQuantity()) {
                throw new ResourceNotFoundException("Book '" + book.getTitle() +
                        "' doesn't have enough available copies. Requested: " +
                        item.getQuantity() + ", Available: " + book.getAvailableCopies());
            }

            // Decrease available copies
            book.setAvailableCopies(book.getAvailableCopies() - item.getQuantity());
            bookRepository.save(book);
        }

        // Update bookshelf status
        bookshelf.setStatus(MyBookshelf.BookshelfStatus.BORROWED);
        bookshelfRepository.save(bookshelf);

        // Update checkout status
        checkout.setStatus(Checkout.CheckoutStatus.BORROWED);

        return checkoutRepository.save(checkout);
    }

    /**
     * Complete return process (all books returned to inventory)
     */
    @Transactional
    public Checkout completeReturn(Long checkoutId) {
        Checkout checkout = getCheckoutById(checkoutId);

        // Validate checkout status
        if (checkout.getStatus() != Checkout.CheckoutStatus.BORROWED &&
                checkout.getStatus() != Checkout.CheckoutStatus.OVERDUE) {
            throw new InvalidOperationException(
                    "Checkout must be in BORROWED or OVERDUE status to be returned");
        }

        MyBookshelf bookshelf = checkout.getBookshelf();

        // Return books to inventory
        for (BookshelfItem item : bookshelf.getBookshelfItems()) {
            Book book = item.getBook();

            // Increase available copies
            book.setAvailableCopies(book.getAvailableCopies() + item.getQuantity());
            bookRepository.save(book);
        }

        // Update bookshelf status
        bookshelf.setStatus(MyBookshelf.BookshelfStatus.COMPLETED);
        bookshelfRepository.save(bookshelf);

        // Update checkout status
        checkout.setStatus(Checkout.CheckoutStatus.RETURNED);
        checkout.setReturnedAt(LocalDateTime.now());

        return checkoutRepository.save(checkout);
    }

    /**
     * Cancel checkout
     */
    @Transactional
    public Checkout cancelCheckout(Long checkoutId) {
        Checkout checkout = getCheckoutById(checkoutId);

        // Only pending checkouts can be cancelled
        if (checkout.getStatus() != Checkout.CheckoutStatus.PENDING) {
            throw new InvalidOperationException(
                    "Only pending checkouts can be cancelled");
        }

        MyBookshelf bookshelf = checkout.getBookshelf();

        // Update bookshelf status back to active
        bookshelf.setStatus(MyBookshelf.BookshelfStatus.ACTIVE);
        bookshelfRepository.save(bookshelf);

        // Update checkout status
        checkout.setStatus(Checkout.CheckoutStatus.CANCELLED);
        checkout.setCancelledAt(LocalDateTime.now());

        return checkoutRepository.save(checkout);
    }

    /**
     * Update checkout status
     */
    @Transactional
    public Checkout updateCheckoutStatus(Long checkoutId, CheckoutStatusUpdateRequest request) {
        Checkout checkout = getCheckoutById(checkoutId);
        Checkout.CheckoutStatus oldStatus = checkout.getStatus();
        Checkout.CheckoutStatus newStatus = request.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);

        checkout.setStatus(newStatus);

        if (request.getAdditionalNotes() != null) {
            checkout.setAdditionalNotes(request.getAdditionalNotes());
        }

        // Update timestamps based on status
        if (newStatus == Checkout.CheckoutStatus.CANCELLED) {
            checkout.setCancelledAt(LocalDateTime.now());
        } else if (newStatus == Checkout.CheckoutStatus.RETURNED) {
            checkout.setReturnedAt(LocalDateTime.now());
        }

        return checkoutRepository.save(checkout);
    }

    /**
     * Get checkout by ID
     */
    public Checkout getCheckoutById(Long checkoutId) {
        return checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new CheckoutException("Checkout Not Found with the id :"+checkoutId));
    }

    /**
     * Get checkout by code
     */
    public Checkout getCheckoutByCode(String checkoutCode) {
        return checkoutRepository.findByCheckoutCode(checkoutCode)
                .orElseThrow(() -> new CheckoutException("Checkout Not Found with the id :"+checkoutCode));
    }

    /**
     * Get all checkouts for a user
     */
    public List<Checkout> getUserCheckouts(Long userId) {
        return checkoutRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Find checkouts with overdue status
     */
    public List<Checkout> findOverdueCheckouts() {
        return checkoutRepository.findOverdueCheckouts(
                LocalDate.now(), Checkout.CheckoutStatus.BORROWED);
    }

    /**
     * Update overdue checkouts
     */
    @Transactional
    public void updateOverdueCheckouts() {
        List<Checkout> overdueCheckouts = findOverdueCheckouts();

        for (Checkout checkout : overdueCheckouts) {
            checkout.setStatus(Checkout.CheckoutStatus.OVERDUE);
            checkoutRepository.save(checkout);
        }
    }

    /**
     * Generate a unique checkout code
     */
    private String generateCheckoutCode() {
        return "BB-" + UUID.randomUUID().toString().substring(0, 15).toUpperCase();
    }

    /**
     * Validate checkout status transition
     */
    private void validateStatusTransition(Checkout.CheckoutStatus oldStatus, Checkout.CheckoutStatus newStatus) {
        boolean valid = switch (oldStatus) {
            case PENDING -> newStatus == Checkout.CheckoutStatus.CONFIRMED ||
                    newStatus == Checkout.CheckoutStatus.CANCELLED ||
                    newStatus == Checkout.CheckoutStatus.BORROWED;
            case CONFIRMED -> newStatus == Checkout.CheckoutStatus.BORROWED;
            case BORROWED -> newStatus == Checkout.CheckoutStatus.RETURNED ||
                    newStatus == Checkout.CheckoutStatus.OVERDUE;
            case OVERDUE -> newStatus == Checkout.CheckoutStatus.RETURNED;
            case RETURNED, CANCELLED ->
                // Terminal states, no further transitions allowed
                    false;
        };

        if (!valid) {
            throw new InvalidOperationException(
                    "Invalid status transition from " + oldStatus + " to " + newStatus);
        }
    }
}
