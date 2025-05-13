package com.BookBliss.Mapper;

import org.springframework.stereotype.Component;
import com.BookBliss.DTO.Admin.CategoryManagement.AdminCategoryDetailsDTO;
import com.BookBliss.Entity.Category;

@Component
public class CategoryMapper {

    /**
     * Maps a Category entity to an AdminCategoryDetailsDTO.
     *
     * @param category The Category entity to map
     * @return The AdminCategoryDetailsDTO
     */
    public AdminCategoryDetailsDTO toAdminDto(Category category) {
        if (category == null) {
            return null;
        }

        return AdminCategoryDetailsDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .bookCount(category.getBookCount())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .build();
    }
}