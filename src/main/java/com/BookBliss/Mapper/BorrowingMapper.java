package com.BookBliss.Mapper;

import com.BookBliss.DTO.Admin.BorrowingManagement.AdminBorrowingDetailsDTO;
import org.springframework.stereotype.Component;

import com.BookBliss.DTO.Borrowing.BorrowingDTO;
import com.BookBliss.Entity.Borrowing;

@Component
public class BorrowingMapper {

 
    public BorrowingDTO toDto(Borrowing borrowing) {
        if (borrowing == null) {
            return null;
        }

        BorrowingDTO dto = new BorrowingDTO();
        dto.setId(borrowing.getId());
        dto.setUserId(borrowing.getUser().getId());
        dto.setUserName(borrowing.getUser().getUsername());
        
        if (borrowing.getBook() != null) {
            dto.setBookId(borrowing.getBook().getId());
            dto.setBookTitle(borrowing.getBook().getTitle());
        }
        
        if (borrowing.getJournal() != null) {
            dto.setJournalId(borrowing.getJournal().getId());
            dto.setJournalTitle(borrowing.getJournal().getTitle());
        }

        dto.setBorrowDate(borrowing.getBorrowDate());
        dto.setDueDate(borrowing.getDueDate());
        dto.setReturnDate(borrowing.getReturnDate());
        dto.setStatus(borrowing.getStatus());
        dto.setFineAmount(borrowing.getFineAmount());

        return dto;
    }

  
    public Borrowing toEntity(BorrowingDTO dto) {
        if (dto == null) {
            return null;
        }

        Borrowing entity = new Borrowing();
        entity.setId(dto.getId());
        entity.setBorrowDate(dto.getBorrowDate());
        entity.setDueDate(dto.getDueDate());
        entity.setReturnDate(dto.getReturnDate());
        entity.setStatus(dto.getStatus());
        entity.setFineAmount(dto.getFineAmount());

        return entity;
    }

   
    public void updateEntityFromDto(BorrowingDTO dto, Borrowing entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getDueDate() != null) {
            entity.setDueDate(dto.getDueDate());
        }
        if (dto.getReturnDate() != null) {
            entity.setReturnDate(dto.getReturnDate());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getFineAmount() != null) {
            entity.setFineAmount(dto.getFineAmount());
        }
    }

    public AdminBorrowingDetailsDTO toAdminDTO(Borrowing borrowing) {
        if(borrowing == null){
            return null;
        }

        return AdminBorrowingDetailsDTO.builder()
                .id(borrowing.getUser().getId())
                .username(borrowing.getUser().getUsername())
                .userEmail(borrowing.getUser().getEmail())
                .itemId(borrowing.getBook().getId())
                .itemType(borrowing.getBook() !=null?"BOOK":"JOURNAL")
                .itemTitle(borrowing.getBook().getTitle())
                .itemAuthor(borrowing.getBook().getAuthor())
                .isbn(borrowing.getBook().getIsbn())
                .issn(borrowing.getBook()!=null?"no issn":"issn")
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .fineAmount(borrowing.getFineAmount())
                .build();
    }


}
