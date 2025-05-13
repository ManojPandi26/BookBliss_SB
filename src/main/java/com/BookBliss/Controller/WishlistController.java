package com.BookBliss.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.BookBliss.Entity.WishlistItem;
import com.BookBliss.Service.Wishlist.WishlistServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistServiceImpl wishlistService;

    @PostMapping("/{userId}/add/{bookId}")
    public ResponseEntity<WishlistItem> addToWishlist(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        WishlistItem wishlistItem = wishlistService.addToWishlist(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistItem);
    }

    @GetMapping("/{userId}")
    
    public ResponseEntity<List<WishlistItem>> getWishlist(@PathVariable Long userId) {
        List<WishlistItem> wishlist = wishlistService.getUserWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{userId}/remove/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        wishlistService.removeFromWishlist(userId, bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/check/{bookId}")
    public ResponseEntity<Boolean> isBookInWishlist(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        boolean isInWishlist = wishlistService.isBookInWishlist(userId, bookId);
        return ResponseEntity.ok(isInWishlist);
    }
}