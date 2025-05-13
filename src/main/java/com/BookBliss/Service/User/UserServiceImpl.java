package com.BookBliss.Service.User;

import com.BookBliss.DTO.Admin.UserManagement.AdminUserDetailsDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserRoleUpdateDTO;
import com.BookBliss.DTO.Admin.UserManagement.UserSearchCriteria;
import com.BookBliss.DTO.Admin.UserManagement.UserStatusUpdateDTO;
import com.BookBliss.DTO.Auth.*;
import com.BookBliss.DTO.UserProfile.UserDetailsDTO;
import com.BookBliss.DTO.UserProfile.UserProfileUpdateDto;
import com.BookBliss.Entity.AuditLog;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.UserNotFoundException;
import com.BookBliss.Mapper.UserMapper;
import com.BookBliss.Repository.UserRepository;
import com.BookBliss.Service.Audit.AuditService;
import com.BookBliss.Service.DropBox.DropboxService;
import com.dropbox.core.DbxException;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class that provides user management functionalities
 * for both regular users and admin users.
 *
 * @author Manoj Pandi
 * @version 1.0
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DropboxService dropboxService;
    private final AuditService auditService;


    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // Admin User Management Methods
    @Override
    public Page<AdminUserDetailsDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        auditService.logActivity(
                AuditLog.EntityType.USER,
                getCurrentUserId(),
                AuditLog.ActionType.READ,
                "Retrieved all users list"
        );
        return users.map(userMapper::toAdminDto);
    }

    @Override
    public Page<AdminUserDetailsDTO> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        Specification<User> spec = buildUserSpecification(criteria);
        Page<User> users = userRepository.findAll(spec, pageable);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                getCurrentUserId(),
                AuditLog.ActionType.READ,
                "Searched users with criteria: " + criteria.toString()
        );

        return users.map(userMapper::toAdminDto);
    }

    private Specification<User> buildUserSpecification(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("fullName")), keyword)
                ));
            }

            if (criteria.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), criteria.getRole()));
            }

            if (criteria.getEmailVerified() != null) {
                predicates.add(cb.equal(root.get("emailVerified"), criteria.getEmailVerified()));
            }

            if (criteria.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            if (criteria.getActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), criteria.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public AdminUserDetailsDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.READ,
                "Retrieved user details for user ID: " + userId
        );

        return userMapper.toAdminDto(user);
    }

    @Transactional
    @Override
    public AdminUserDetailsDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        User.UserRole oldRole = user.getRole();
        user.setRole(roleUpdateDTO.getRole());
        User updatedUser = userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.ROLE_CHANGE,
                "Changed user role from " + oldRole + " to " + roleUpdateDTO.getRole() + " for user ID: " + userId
        );

        // Optionally send email notification to user
       // emailService.sendRoleChangeNotification(user.getEmail(), user.getFullName(), oldRole, roleUpdateDTO.getRole());

        return userMapper.toAdminDto(updatedUser);
    }

    @Transactional
    @Override
    public AdminUserDetailsDTO updateUserStatus(Long userId, UserStatusUpdateDTO statusUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean oldStatus = user.isActive();
        user.setActive(statusUpdateDTO.isActive());
        User updatedUser = userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.STATUS_CHANGE,
                "Changed user status from " + oldStatus + " to " + statusUpdateDTO.isActive() + " for user ID: " + userId
        );

        // Optionally send email notification
//        if (oldStatus && !statusUpdateDTO.isActive()) {
//            emailService.sendAccountDeactivationNotification(user.getEmail(), user.getFullName());
//        } else if (!oldStatus && statusUpdateDTO.isActive()) {
//            emailService.sendAccountReactivationNotification(user.getEmail(), user.getFullName());
//        }


        return userMapper.toAdminDto(updatedUser);
    }

    @Transactional
    @Override
    public void resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Generate a random secure password
        String generatedPassword = RandomStringUtils.randomAlphanumeric(12);
        user.setPassword(passwordEncoder.encode(generatedPassword));
        userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.PASSWORD_CHANGE,
                "Admin reset password for user ID: " + userId
        );

        // Send email with temporary password
      //  emailService.sendPasswordResetNotification(user.getEmail(), user.getFullName(), generatedPassword);
    }

    @Transactional
    @Override
    public void manuallyVerifyUserEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean oldVerification = user.isEmailVerified();
        user.setEmailVerified(true);
        userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.EMAIL_VERIFY,
                "Manually verified email for user ID: " + userId
        );

//        if (!oldVerification) {
//            emailService.sendEmailVerificationConfirmation(user.getEmail(), user.getFullName());
//        }
    }

    // Existing methods with audit logging added

    @Override
    public UserDetailsDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.READ,
                "User profile viewed"
        );

        return userMapper.toProfileDto(user);
    }

    @Transactional
    @Override
    public UserDetailsDTO updateUserProfile(Long userId, UserProfileUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        StringBuilder changes = new StringBuilder();

        // Update basic profile information
        if (updateDto.getFullName() != null && !updateDto.getFullName().equals(user.getFullName())) {
            changes.append("Name changed from '").append(user.getFullName())
                    .append("' to '").append(updateDto.getFullName()).append("'. ");
            user.setFullName(updateDto.getFullName());
        }

        if (updateDto.getPhoneNumber() != null && !updateDto.getPhoneNumber().equals(user.getPhoneNumber())) {
            changes.append("Phone changed from '").append(user.getPhoneNumber())
                    .append("' to '").append(updateDto.getPhoneNumber()).append("'. ");
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }

        if (updateDto.getAddress() != null && !updateDto.getAddress().equals(user.getAddress())) {
            changes.append("Address updated. ");
            user.setAddress(updateDto.getAddress());
        }

        User updatedUser = userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.UPDATE,
                "Profile updated: " + changes.toString()
        );

        return userMapper.toProfileDto(updatedUser);
    }

    @Transactional
    @Override
    public UserDetailsDTO updateUserProfileImage(Long userId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new InvalidOperationException("Image file cannot be empty");
        }

        // Validate file type
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidOperationException("Only image files are allowed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            // Delete old profile image if exists
            if (user.getProfileImageUrl() != null) {
                String oldPath = dropboxService.extractPathFromUrl(user.getProfileImageUrl());
                if (oldPath != null) {
                    try {
                        dropboxService.deleteFile(oldPath);
                    } catch (DbxException e) {
                        log.warn("Failed to delete old profile image: {}", e.getMessage());
                    }
                }
            }

            // Upload new image
            String imageUrl = dropboxService.uploadFile(imageFile, "profile");
            user.setProfileImageUrl(imageUrl);

            User updatedUser = userRepository.save(user);

            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    userId,
                    AuditLog.ActionType.UPDATE,
                    "Profile image updated"
            );

            return userMapper.toProfileDto(updatedUser);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void deleteUserProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getProfileImageUrl() != null) {
            try {
                String path = dropboxService.extractPathFromUrl(user.getProfileImageUrl());
                if (path != null) {
                    dropboxService.deleteFile(path);
                }
                user.setProfileImageUrl(null);
                userRepository.save(user);

                auditService.logActivity(
                        AuditLog.EntityType.USER,
                        userId,
                        AuditLog.ActionType.DELETE,
                        "Profile image deleted"
                );

            } catch (DbxException e) {
                throw new RuntimeException("Failed to delete profile image: " + e.getMessage(), e);
            }
        }
    }

    @Transactional
    @Override
    public void updatePassword(Long userId, PasswordUpdateDto passwordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        if (passwordDto.getNewPassword().equals(passwordDto.getCurrentPassword())) {
            throw new InvalidOperationException("New password cannot be the same as the old password");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.PASSWORD_CHANGE,
                "User changed their password"
        );
    }

    //Admin controls
    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Handle image deletion if needed
        if (user.getProfileImageUrl() != null) {
            try {
                String path = dropboxService.extractPathFromUrl(user.getProfileImageUrl());
                if (path != null) {
                    dropboxService.deleteFile(path);
                }
            } catch (DbxException e) {
                log.warn("Failed to delete profile image during user deletion: {}", e.getMessage());
            }
        }

        // Capture user details for audit log before deletion
        String username = user.getUsername();
        String email = user.getEmail();

        userRepository.delete(user);

        auditService.logActivity(
                AuditLog.EntityType.USER,
                userId,
                AuditLog.ActionType.DELETE,
                "User deleted. Username: " + username + ", Email: " + email
        );
    }

    // Utility methods
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User user= userRepository.findByUsername(authentication.getName()).orElseThrow(()-> new UserNotFoundException("User Not Found"));
            return user.getId();
        }
        return null;
    }

    // Additional enterprise functionalities

    @Transactional
    public void bulkUpdateUserStatus(List<Long> userIds, boolean active) {
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            boolean oldStatus = user.isActive();
            user.setActive(active);

            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.STATUS_CHANGE,
                    "Bulk status change from " + oldStatus + " to " + active
            );

            // Optional: Send email notifications
//            if (oldStatus && !active) {
//                emailService.sendAccountDeactivationNotification(user.getEmail(), user.getFullName());
//            } else if (!oldStatus && active) {
//                emailService.sendAccountReactivationNotification(user.getEmail(), user.getFullName());
//            }
        }

        userRepository.saveAll(users);
    }

    @Transactional
    public void bulkDeleteInactiveUsers(LocalDateTime lastActiveDate) {
        List<User> inactiveUsers = userRepository.findInactiveUsers(lastActiveDate);

        for (User user : inactiveUsers) {
            // Handle profile images if needed
            if (user.getProfileImageUrl() != null) {
                try {
                    String path = dropboxService.extractPathFromUrl(user.getProfileImageUrl());
                    if (path != null) {
                        dropboxService.deleteFile(path);
                    }
                } catch (DbxException e) {
                    log.warn("Failed to delete profile image during user deletion: {}", e.getMessage());
                }
            }

            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.DELETE,
                    "Inactive user automatically deleted. Last login: " + user.getLastLogin()
            );
        }

        userRepository.deleteAll(inactiveUsers);
    }

    @Transactional
    public void lockUnverifiedAccounts(LocalDateTime cutoffDate) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("emailVerified"), false));
            predicates.add(cb.lessThan(root.get("createdAt"), cutoffDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<User> unverifiedUsers = userRepository.findAll((Sort) spec);

        for (User user : unverifiedUsers) {
            user.setActive(false);

            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.STATUS_CHANGE,
                    "Account locked due to unverified email after " +
                            cutoffDate.toString()
            );

            // Notify users
//            emailService.sendAccountLockNotification(
//                    user.getEmail(),
//                    user.getFullName(),
//                    "Email verification pending"
//            );
        }

        userRepository.saveAll(unverifiedUsers);
    }

}