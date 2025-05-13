package com.BookBliss.DTO.Books;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookAddingDTO {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Author is mandatory")
    private String author;

    @NotBlank(message = "Publisher is mandatory")
    private String publisher;

    @NotNull(message = "Publication year is mandatory")
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2024, message = "Publication year cannot be in the future")
    private Integer publicationYear;

    @NotBlank(message = "ISBN is mandatory")
    @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})([0-9]+[- ]){3}[0-9]+$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$", 
    message = "Invalid ISBN format")
    private String isbn;

    private String edition;

    private String description;

    @Positive(message = "Available copies must be greater than 0")
    private int availableCopies;

    @Positive(message = "Total copies must be greater than 0")
    private int totalCopies;
    
    private List<String> categories;
}
