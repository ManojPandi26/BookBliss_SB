package com.BookBliss.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean used = false;

    // Constructor for creating a new token
    public PasswordResetToken(User user, String token) {
        this.user = user;
        this.token = token;
        // Set expiry to 1 hour from now
        this.expiryDate = LocalDateTime.now().plusMinutes(10);
    }

    // Check if token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
