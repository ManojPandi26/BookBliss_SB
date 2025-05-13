package com.BookBliss.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.BookBliss.DTO.Admin.BookManagement.AdminBookDetailsDTO;
import com.BookBliss.DTO.Books.BookPreviewDTO;
import com.BookBliss.DTO.Books.BookSummaryDTO;
import org.springframework.stereotype.Component;

import com.BookBliss.DTO.Books.BookAddingDTO;
import com.BookBliss.DTO.Books.BookDetailsDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Category;

@Component
public class BookMapper {

	public Book convertToEntity(BookAddingDTO bookAddingDTO) {
        Book book = new Book();
        book.setTitle(bookAddingDTO.getTitle());
        book.setAuthor(bookAddingDTO.getAuthor());
        book.setPublisher(bookAddingDTO.getPublisher());
        book.setPublicationYear(bookAddingDTO.getPublicationYear());
        book.setIsbn(bookAddingDTO.getIsbn());
        book.setEdition(bookAddingDTO.getEdition());
        book.setDescription(bookAddingDTO.getDescription());
        book.setAvailableCopies(bookAddingDTO.getAvailableCopies());
        book.setTotalCopies(bookAddingDTO.getTotalCopies());
        
        return book;
    }
	
	
	// Convert Set<Category> to List<String> (for DTO purposes)
    public List<String> convertCategoriesToList(Set<Category> categories) {
        if(categories == null){
            return List.of();
        }
        return categories.stream()
                         .map(Category::getName)
                         .collect(Collectors.toList());
    }
	
	
	public BookDetailsDTO convertToDetailsDTO(Book book) {
        BookDetailsDTO dto = new BookDetailsDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setIsbn(book.getIsbn());
        dto.setEdition(book.getEdition());
        dto.setDescription(book.getDescription());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setTotalCopies(book.getTotalCopies());
        List<String> categoryNames = convertCategoriesToList(book.getCategories());
        dto.setCategories(categoryNames);
        return dto;
    }

    public BookPreviewDTO convertToPreviewDTO(Book book){
        if (book == null) return null;
        List<String> categoryNames = convertCategoriesToList(book.getCategories());
        return BookPreviewDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImageUrl(book.getCoverImageUrl())
                .availableCopies(book.getAvailableCopies())
                .categories(categoryNames)
                .build();
    }

    public AdminBookDetailsDTO toAdminBookDetailsDTO(Book book) {
        if (book == null) {
            return null;
        }

        return AdminBookDetailsDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .isbn(book.getIsbn())
                .edition(book.getEdition())
                .description(book.getDescription())
                .coverImageUrl(book.getCoverImageUrl())
                .availableCopies(book.getAvailableCopies())
                .totalCopies(book.getTotalCopies())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .categories(this.convertCategoriesToList(book.getCategories()))
                .build();
    }

    public BookSummaryDTO convertToSummaryDTO(Book book){
        List<String> categoryNames = convertCategoriesToList(book.getCategories());
        return BookSummaryDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .coverImageUrl(book.getCoverImageUrl())
                .availableCopies(book.getAvailableCopies())
                .categories(categoryNames)
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }

}
