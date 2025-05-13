package com.BookBliss.Service.User;

import com.BookBliss.DTO.Admin.UserManagement.AdminUserDetailsDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserRoleUpdateDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserSearchCriteria;
import com.BookBliss.DTO.Admin.UserManagement.UserStatusUpdateDTO;
import com.BookBliss.DTO.Auth.*;
import com.BookBliss.DTO.UserProfile.UserDetailsDTO;
import com.BookBliss.DTO.UserProfile.UserProfileUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    // User profile methods
    UserDetailsDTO getUserProfile(Long userId);
    UserDetailsDTO updateUserProfile(Long userId, UserProfileUpdateDto updateDto);
    UserDetailsDTO updateUserProfileImage(Long userId, MultipartFile imageFile);
    void deleteUserProfileImage(Long userId);
    void updatePassword(Long userId, PasswordUpdateDto passwordDto);

    // Admin methods
    Page<AdminUserDetailsDTO> getAllUsers(Pageable pageable);
    Page<AdminUserDetailsDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable);
    AdminUserDetailsDTO getUserById(Long userId);
    AdminUserDetailsDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO);
    AdminUserDetailsDTO updateUserStatus(Long userId, UserStatusUpdateDTO statusUpdateDTO);
    void resetUserPassword(Long userId);
    void manuallyVerifyUserEmail(Long userId);
    void deleteUser(Long userId);

    // Bulk operations
    void bulkUpdateUserStatus(List<Long> userIds, boolean active);
    void bulkDeleteInactiveUsers(LocalDateTime lastActiveDate);
    void lockUnverifiedAccounts(LocalDateTime cutoffDate);

}
