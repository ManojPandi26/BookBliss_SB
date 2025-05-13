package com.BookBliss.Mapper;


import com.BookBliss.DTO.JournalDTO;
import com.BookBliss.Entity.Journal;


import org.springframework.stereotype.Component;

@Component
public class JournalMapper {
    
    public JournalDTO toDto(Journal journal) {
        if (journal == null) {
            return null;
        }

        JournalDTO dto = new JournalDTO();
        dto.setId(journal.getId());
        dto.setTitle(journal.getTitle());
        dto.setPublisher(journal.getPublisher());
        dto.setIssueNumber(journal.getIssueNumber());
        dto.setVolumeNumber(journal.getVolumeNumber());
        dto.setPublicationDate(journal.getPublicationDate());
        dto.setDescription(journal.getDescription());
        dto.setCoverImageUrl(journal.getCoverImageUrl());
        dto.setAvailableCopies(journal.getAvailableCopies());
        dto.setTotalCopies(journal.getTotalCopies());
        dto.setCreatedAt(journal.getCreatedAt());
        dto.setUpdatedAt(journal.getUpdatedAt());
        
        return dto;
    }

    public Journal toEntity(JournalDTO dto) {
        if (dto == null) {
            return null;
        }

        Journal journal = new Journal();
        journal.setId(dto.getId());
        journal.setTitle(dto.getTitle());
        journal.setPublisher(dto.getPublisher());
        journal.setIssueNumber(dto.getIssueNumber());
        journal.setVolumeNumber(dto.getVolumeNumber());
        journal.setPublicationDate(dto.getPublicationDate());
        journal.setDescription(dto.getDescription());
        journal.setCoverImageUrl(dto.getCoverImageUrl());
        journal.setAvailableCopies(dto.getAvailableCopies());
        journal.setTotalCopies(dto.getTotalCopies());
        
        return journal;
    }

    public void updateEntityFromDto(JournalDTO dto, Journal journal) {
        if (dto == null || journal == null) {
            return;
        }

        if (dto.getTitle() != null) {
            journal.setTitle(dto.getTitle());
        }
        if (dto.getPublisher() != null) {
            journal.setPublisher(dto.getPublisher());
        }
        if (dto.getIssueNumber() != null) {
            journal.setIssueNumber(dto.getIssueNumber());
        }
        if (dto.getVolumeNumber() != null) {
            journal.setVolumeNumber(dto.getVolumeNumber());
        }
        if (dto.getPublicationDate() != null) {
            journal.setPublicationDate(dto.getPublicationDate());
        }
        if (dto.getDescription() != null) {
            journal.setDescription(dto.getDescription());
        }
        if (dto.getCoverImageUrl() != null) {
            journal.setCoverImageUrl(dto.getCoverImageUrl());
        }
        if (dto.getAvailableCopies() != null) {
            journal.setAvailableCopies(dto.getAvailableCopies());
        }
        if (dto.getTotalCopies() != null) {
            journal.setTotalCopies(dto.getTotalCopies());
        }
    }
}