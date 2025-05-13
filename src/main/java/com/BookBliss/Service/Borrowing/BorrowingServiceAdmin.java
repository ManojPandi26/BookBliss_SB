package com.BookBliss.Service.Borrowing;

import com.BookBliss.DTO.Admin.BorrowingManagement.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BorrowingServiceAdmin {

    Page<AdminBorrowingDetailsDTO> getAllBorrowingsForAdmin(Pageable pageable);
    Page<AdminBorrowingDetailsDTO> searchBorrowings(BorrowingSearchCriteria criteria, Pageable pageable);
    AdminBorrowingDetailsDTO getAdminBorrowingDetails(Long borrowingId);
    AdminBorrowingDetailsDTO updateBorrowingStatus(Long borrowingId, BorrowingStatusUpdateDTO statusUpdateDTO);
  //  AdminBorrowingDetailsDTO extendBorrowingDueDate(Long borrowingId, BorrowingExtensionDTO extensionDTO);
    AdminBorrowingDetailsDTO processBookReturn(Long borrowingId);
    AdminBorrowingDetailsDTO adjustFineAmount(Long borrowingId, BigDecimal fineAmount);
    AdminBorrowingDetailsDTO waiveFine(Long borrowingId);
    Page<AdminBorrowingDetailsDTO> getOverdueBorrowings(Pageable pageable);
    BorrowingStatisticsDTO getBorrowingStatistics();
    void deleteBorrowingByAdmin(Long borrowingId);
}
