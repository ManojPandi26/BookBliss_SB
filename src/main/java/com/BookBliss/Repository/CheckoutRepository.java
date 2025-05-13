package com.BookBliss.Repository;

import com.BookBliss.Entity.Checkout;
import com.BookBliss.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Long> {

    List<Checkout> findByUserOrderByCreatedAtDesc(User user);

    Optional<Checkout> findByCheckoutCode(String checkoutCode);

    List<Checkout> findByStatus(Checkout.CheckoutStatus status);

    @Query("SELECT c FROM Checkout c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Checkout> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT c FROM Checkout c WHERE c.dueDate < :currentDate AND c.status = :status")
    List<Checkout> findOverdueCheckouts(@Param("currentDate") LocalDate currentDate, @Param("status") Checkout.CheckoutStatus status);

    @Query("SELECT c FROM Checkout c WHERE c.bookshelf.id = :bookshelfId")
    Optional<Checkout> findByBookshelfId(@Param("bookshelfId") Long bookshelfId);

    @Query("SELECT COUNT(c) FROM Checkout c WHERE c.user.id = :userId AND c.status = :status")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Checkout.CheckoutStatus status);
}
