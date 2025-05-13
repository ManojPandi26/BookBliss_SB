package com.BookBliss.Service.Wishlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.User;
import com.BookBliss.Entity.WishlistItem;
import com.BookBliss.Exception.ResourceNotFoundException;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.UserRepository;
import com.BookBliss.Repository.WishlistRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    @Override
    public WishlistItem addToWishlist(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            
        // Check if already exists
        Optional<WishlistItem> existingItem = wishlistRepository
            .findByUserIdAndBookId(userId, bookId);
            
        if (existingItem.isPresent()) {
            return existingItem.get();
        }
        
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setUser(user);
        wishlistItem.setBook(book);
        wishlistItem.setAddedAt(LocalDateTime.now());
        
        return wishlistRepository.save(wishlistItem);
    }

    @Override
    public List<WishlistItem> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public void removeFromWishlist(Long userId, Long bookId) {
        WishlistItem wishlistItem = wishlistRepository.findByUserIdAndBookId(userId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        wishlistRepository.delete(wishlistItem);
    }

   @Override
   public boolean isBookInWishlist(Long userId, Long bookId) {
        return wishlistRepository.existsByUserIdAndBookId(userId, bookId);
    }

    /**
     * Gets the count of wishlist entries for a specific book
     *
     * @param bookId the ID of the book to get wishlist count for
     * @return the number of users who have added this book to their wishlist
     */
    public Long getBookWishlistCount(Long bookId) {
        return wishlistRepository.countByBookId(bookId);
    }
}

