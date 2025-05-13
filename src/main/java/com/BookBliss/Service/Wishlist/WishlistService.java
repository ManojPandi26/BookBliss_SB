package com.BookBliss.Service.Wishlist;

import com.BookBliss.Entity.WishlistItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WishlistService {
    @Transactional
    WishlistItem addToWishlist(Long userId, Long bookId);

    List<WishlistItem> getUserWishlist(Long userId);

    @Transactional
    void removeFromWishlist(Long userId, Long bookId);

    boolean isBookInWishlist(Long userId, Long bookId);
}
