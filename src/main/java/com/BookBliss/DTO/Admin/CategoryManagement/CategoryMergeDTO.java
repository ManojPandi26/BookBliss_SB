package com.BookBliss.DTO.Admin.CategoryManagement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMergeDTO {

    @NotNull(message = "Source category ID is required")
    private Long sourceCategoryId;

    @NotNull(message = "Target category ID is required")
    private Long targetCategoryId;

    private boolean deleteSourceAfterMerge;
}
