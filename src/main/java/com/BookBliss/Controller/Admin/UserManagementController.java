package com.BookBliss.Controller.Admin;

import com.BookBliss.DTO.Admin.UserManagement.AdminUserDetailsDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserRoleUpdateDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserSearchCriteria;
import com.BookBliss.DTO.Admin.UserManagement.UserStatusUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import com.BookBliss.Service.User.UserServiceImpl;
import com.BookBliss.Service.Audit.AuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserServiceImpl userService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDetailsDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AdminUserDetailsDTO>> searchUsers(
            @RequestBody UserSearchCriteria searchCriteria,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(searchCriteria, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailsDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<AdminUserDetailsDTO> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateDTO roleUpdateDTO) {
        return ResponseEntity.ok(userService.updateUserRole(userId, roleUpdateDTO));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<AdminUserDetailsDTO> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, statusUpdateDTO));
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long userId) {
        userService.resetUserPassword(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/audit-logs")
    public ResponseEntity<?> getUserAuditLogs(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getUserAuditLogs(userId, pageable));
    }

    @PostMapping("/{userId}/verify-email")
    public ResponseEntity<Void> manuallyVerifyUserEmail(@PathVariable Long userId) {
        userService.manuallyVerifyUserEmail(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}