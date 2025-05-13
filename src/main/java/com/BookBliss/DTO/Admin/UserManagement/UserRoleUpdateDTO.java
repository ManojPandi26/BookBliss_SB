package com.BookBliss.DTO.Admin.UserManagement;


import com.BookBliss.Entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateDTO {
    @NotNull(message = "Role is required")
    private User.UserRole role;
}
