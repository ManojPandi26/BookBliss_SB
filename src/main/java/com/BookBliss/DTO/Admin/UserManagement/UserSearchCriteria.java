package com.BookBliss.DTO.Admin.UserManagement;

import com.BookBliss.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {
    private String keyword;
    private User.UserRole role;
    private Boolean emailVerified;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private Boolean active;
}
