package com.BookBliss.Repository;

import com.BookBliss.Entity.Token;
import com.BookBliss.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValueAndTokenType(String tokenValue, Token.TokenType tokenType);

    Optional<Token> findByTokenIdentifier(String tokenIdentifier);

    List<Token> findAllByUserAndTokenTypeAndRevokedFalse(User user, Token.TokenType tokenType);

    @Query("SELECT t FROM Token t WHERE t.user = :user AND t.tokenType = :tokenType AND t.revoked = false AND t.expiresAt > :now")
    List<Token> findValidTokensByUser(@Param("user") User user, @Param("tokenType") Token.TokenType tokenType, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true, t.revokedAt = :now WHERE t.user = :user AND t.tokenType = :tokenType AND t.revoked = false")
    int revokeAllUserTokens(@Param("user") User user, @Param("tokenType") Token.TokenType tokenType, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true, t.revokedAt = :now WHERE t.tokenValue = :tokenValue")
    int revokeToken(@Param("tokenValue") String tokenValue, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    int deleteAllExpiredTokens(@Param("now") LocalDateTime now);

    boolean existsByTokenValueAndRevokedFalse(String tokenValue);
}