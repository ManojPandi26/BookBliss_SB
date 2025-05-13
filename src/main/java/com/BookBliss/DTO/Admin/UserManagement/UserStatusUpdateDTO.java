package com.BookBliss.DTO.Admin.UserManagement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateDTO {
    @NotNull(message = "Active status is required")
    private boolean active;
}