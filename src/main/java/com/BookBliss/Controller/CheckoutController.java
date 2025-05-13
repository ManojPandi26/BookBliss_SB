package com.BookBliss.Controller;

import com.BookBliss.DTO.CheckOut.CheckoutDTOs;
import com.BookBliss.Entity.Checkout;
import com.BookBliss.Service.Checkout.CheckoutServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/checkouts")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout management APIs")
public class CheckoutController {

    private final CheckoutServiceImpl checkoutService;

    @PostMapping
    @Operation(summary = "Initiate checkout process")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> initiateCheckout(
            @Valid @RequestBody CheckoutDTOs.CheckoutRequest request,
            @RequestParam Long userId) {

        Checkout checkout = checkoutService.initiateCheckout(userId, request);
        return new ResponseEntity<>(CheckoutDTOs.CheckoutResponse.fromEntity(checkout), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Get all checkouts for current user")
    public ResponseEntity<List<CheckoutDTOs.CheckoutResponse>> getUserCheckouts(@RequestParam Long userId) {

        List<Checkout> checkouts = checkoutService.getUserCheckouts(userId);

        List<CheckoutDTOs.CheckoutResponse> response = checkouts.stream()
                .map(CheckoutDTOs.CheckoutResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkout by ID")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> getCheckoutById(@PathVariable Long id) {
        Checkout checkout = checkoutService.getCheckoutById(id);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get checkout by code")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> getCheckoutByCode(@PathVariable String code) {
        Checkout checkout = checkoutService.getCheckoutByCode(code);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Confirm checkout (librarian/admin only)")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> confirmCheckout(@PathVariable Long id) {
        Checkout checkout = checkoutService.confirmCheckout(id);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Process return (librarian/admin only)")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> processReturn(@PathVariable Long id) {
        Checkout checkout = checkoutService.completeReturn(id);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel checkout")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> cancelCheckout(@PathVariable Long id) {
        Checkout checkout = checkoutService.cancelCheckout(id);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update checkout status (librarian/admin only)")
    public ResponseEntity<CheckoutDTOs.CheckoutResponse> updateCheckoutStatus(
            @PathVariable Long id,
            @Valid @RequestBody CheckoutDTOs.CheckoutStatusUpdateRequest request) {
        Checkout checkout = checkoutService.updateCheckoutStatus(id, request);
        return ResponseEntity.ok(CheckoutDTOs.CheckoutResponse.fromEntity(checkout));
    }

    @PostMapping("/update-overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update overdue checkouts (librarian/admin only)")
    public ResponseEntity<Void> updateOverdueCheckouts() {
        checkoutService.updateOverdueCheckouts();
        return ResponseEntity.ok().build();
    }

}
