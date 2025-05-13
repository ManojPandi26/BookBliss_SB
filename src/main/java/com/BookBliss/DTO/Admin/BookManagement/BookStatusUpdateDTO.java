package com.BookBliss.DTO.Admin.BookManagement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatusUpdateDTO {
    @NotBlank(message = "Status cannot be blank")
    private String status;

    private String reason;
}
