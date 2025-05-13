package com.BookBliss.Service.Borrowing;

import com.BookBliss.DTO.Admin.BorrowingManagement.AdminBorrowingDetailsDTO;
import com.BookBliss.DTO.Admin.BorrowingManagement.BorrowingSearchCriteria;
import com.BookBliss.DTO.Admin.BorrowingManagement.BorrowingStatisticsDTO;
import com.BookBliss.DTO.Admin.BorrowingManagement.BorrowingStatusUpdateDTO;
import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.Borrowing;
import com.BookBliss.Entity.Journal;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.InvalidOperationException;
import com.BookBliss.Exception.ResourceNotFoundException;

import com.BookBliss.Mapper.BorrowingMapper;
import com.BookBliss.Repository.BookRepository;
import com.BookBliss.Repository.BorrowingRepository;
import com.BookBliss.Repository.JournalRepository;
import com.BookBliss.Repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class BorrowingServiceAdminImpl implements BorrowingServiceAdmin{
    private static final int MAX_EXTENSION_COUNT = 3;
    private static final int DEFAULT_EXTENSION_DAYS = 7;
    private static final BigDecimal FINE_RATE_PER_DAY = new BigDecimal("10.0");

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final JournalRepository journalRepository;
    private final BorrowingMapper adminBorrowingMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AdminBorrowingDetailsDTO> getAllBorrowingsForAdmin(Pageable pageable) {
        log.info("Fetching all borrowings for admin with page: {} and size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Borrowing> borrowingsPage = borrowingRepository.findAll(pageable);

        List<AdminBorrowingDetailsDTO> borrowingDetailsDTOs = borrowingsPage.getContent().stream()
                .map(this::mapToAdminBorrowingDetailsDTO)
                .toList();

        return new PageImpl<>(borrowingDetailsDTOs, pageable, borrowingsPage.getTotalElements());
    }

    @Override
    public Page<AdminBorrowingDetailsDTO> searchBorrowings(BorrowingSearchCriteria criteria, Pageable pageable) {
        log.info("Searching borrowings with criteria: {}", criteria);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Borrowing> query = cb.createQuery(Borrowing.class);
        Root<Borrowing> borrowing = query.from(Borrowing.class);

        Join<Borrowing, User> userJoin = borrowing.join("user", JoinType.INNER);
        Join<Borrowing, Book> bookJoin = borrowing.join("book", JoinType.LEFT);
        Join<Borrowing, Journal> journalJoin = borrowing.join("journal", JoinType.LEFT);

        List<Predicate> predicates = createPredicates(criteria, cb, borrowing, userJoin, bookJoin, journalJoin);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Handle dynamic sorting if provided in criteria
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            String sortField = criteria.getSortBy();
            boolean isAscending = criteria.getSortDirection() == null ||
                    "asc".equalsIgnoreCase(criteria.getSortDirection());

            // Determine which join to use based on the sort field
            Path<?> sortPath;
            if (Arrays.asList("username", "email").contains(sortField)) {
                sortPath = userJoin.get(sortField);
            } else if (Arrays.asList("title", "author", "publisher", "isbn").contains(sortField)) {
                sortPath = bookJoin.get(sortField);
            } else if (Arrays.asList("title", "publisher", "issn").contains(sortField)) {
                sortPath = journalJoin.get(sortField);
            } else {
                // Default to borrowing entity fields
                sortPath = borrowing.get(sortField);
            }

            if (isAscending) {
                query.orderBy(cb.asc(sortPath));
            } else {
                query.orderBy(cb.desc(sortPath));
            }
        } else {
            // Default sort by borrow date descending
            query.orderBy(cb.desc(borrowing.get("borrowDate")));
        }

        // Execute query with pagination
        TypedQuery<Borrowing> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Borrowing> borrowings = typedQuery.getResultList();

        // Count total results for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Borrowing> countRoot = countQuery.from(Borrowing.class);
        countQuery.select(cb.count(countRoot));

        // Apply same joins and predicates for count query
        Join<Borrowing, User> countUserJoin = countRoot.join("user", JoinType.INNER);
        Join<Borrowing, Book> countBookJoin = countRoot.join("book", JoinType.LEFT);
        Join<Borrowing, Journal> countJournalJoin = countRoot.join("journal", JoinType.LEFT);

        // Recreate predicates for count query
        List<Predicate> countPredicates = createPredicates(criteria, cb, countRoot, countUserJoin, countBookJoin, countJournalJoin);
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        // Map to DTOs
        List<AdminBorrowingDetailsDTO> borrowingDetailsDTOs = borrowings.stream()
                .map(this::mapToAdminBorrowingDetailsDTO)
                .toList();

        return new PageImpl<>(borrowingDetailsDTOs, pageable, totalCount);
    }

    /**
     * Helper method to create predicates for borrowing search queries.
     * This avoids duplication between main query and count query.
     */
    private List<Predicate> createPredicates(
            BorrowingSearchCriteria criteria,
            CriteriaBuilder cb,
            Root<Borrowing> borrowing,
            Join<Borrowing, User> userJoin,
            Join<Borrowing, Book> bookJoin,
            Join<Borrowing, Journal> journalJoin) {

        List<Predicate> predicates = new ArrayList<>();

        // User keyword search
        if (criteria.getUserKeyword() != null && !criteria.getUserKeyword().trim().isEmpty()) {
            String keyword = "%" + criteria.getUserKeyword().toLowerCase().trim() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(userJoin.get("username")), keyword),
                    cb.like(cb.lower(userJoin.get("email")), keyword)
            ));
        }

        // Item keyword search
        if (criteria.getItemKeyword() != null && !criteria.getItemKeyword().trim().isEmpty()) {
            String keyword = "%" + criteria.getItemKeyword().toLowerCase().trim() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(bookJoin.get("title")), keyword),
                    cb.like(cb.lower(bookJoin.get("author")), keyword),
                    cb.like(cb.lower(bookJoin.get("publisher")), keyword),
                    cb.like(cb.lower(bookJoin.get("isbn")), keyword),
                    cb.like(cb.lower(journalJoin.get("title")), keyword),
                    cb.like(cb.lower(journalJoin.get("publisher")), keyword),
                    cb.like(cb.lower(journalJoin.get("issn")), keyword)
            ));
        }

        // Book specific criteria
        if (criteria.getBookId() != null) {
            predicates.add(cb.equal(bookJoin.get("id"), criteria.getBookId()));
        }

        if (criteria.getIsbn() != null && !criteria.getIsbn().isEmpty()) {
            predicates.add(cb.like(bookJoin.get("isbn"), "%" + criteria.getIsbn() + "%"));
        }

        // Journal specific criteria
        if (criteria.getJournalId() != null) {
            predicates.add(cb.equal(journalJoin.get("id"), criteria.getJournalId()));
        }

        if (criteria.getIssn() != null && !criteria.getIssn().isEmpty()) {
            predicates.add(cb.like(journalJoin.get("issn"), "%" + criteria.getIssn() + "%"));
        }

        // Item type criteria
        if (criteria.getItemType() != null) {
            if ("BOOK".equalsIgnoreCase(criteria.getItemType())) {
                predicates.add(cb.isNotNull(borrowing.get("book")));
                predicates.add(cb.isNull(borrowing.get("journal")));
            } else if ("JOURNAL".equalsIgnoreCase(criteria.getItemType())) {
                predicates.add(cb.isNull(borrowing.get("book")));
                predicates.add(cb.isNotNull(borrowing.get("journal")));
            }
        }

        // Status criteria
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            predicates.add(borrowing.get("status").in(criteria.getStatuses()));
        }

        // Date criteria
        if (criteria.getBorrowDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(borrowing.get("borrowDate"),
                    criteria.getBorrowDateFrom()));
        }

        if (criteria.getBorrowDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(borrowing.get("borrowDate"),
                    criteria.getBorrowDateTo()));
        }

        if (criteria.getDueDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(borrowing.get("dueDate"),
                    criteria.getDueDateFrom()));
        }

        if (criteria.getDueDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(borrowing.get("dueDate"),
                    criteria.getDueDateTo()));
        }

        if (criteria.getReturnDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(borrowing.get("returnDate"),
                    criteria.getReturnDateFrom()));
        }

        if (criteria.getReturnDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(borrowing.get("returnDate"),
                    criteria.getReturnDateTo()));
        }

        // Fine criteria
        if (criteria.getHasOverdueFine() != null) {
            if (criteria.getHasOverdueFine()) {
                predicates.add(cb.isNotNull(borrowing.get("fineAmount")));
                predicates.add(cb.greaterThan(borrowing.get("fineAmount"), BigDecimal.ZERO));
            } else {
                Predicate nullFine = cb.isNull(borrowing.get("fineAmount"));
                Predicate zeroFine = cb.equal(borrowing.get("fineAmount"), BigDecimal.ZERO);
                predicates.add(cb.or(nullFine, zeroFine));
            }
        }

        if (criteria.getIsCurrentlyOverdue() != null && criteria.getIsCurrentlyOverdue()) {
            predicates.add(cb.lessThan(borrowing.get("dueDate"), LocalDateTime.now()));
            predicates.add(cb.equal(borrowing.get("status"), Borrowing.BorrowingStatus.BORROWED));
        }

        // Extension criteria
        if (criteria.getMinExtensionCount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(borrowing.get("extensionCount"),
                    criteria.getMinExtensionCount()));
        }

        if (criteria.getMaxExtensionCount() != null) {
            predicates.add(cb.lessThanOrEqualTo(borrowing.get("extensionCount"),
                    criteria.getMaxExtensionCount()));
        }

        // Fine waived criteria
        if (criteria.getFineWaived() != null) {
            predicates.add(cb.equal(borrowing.get("fineWaived"), criteria.getFineWaived()));
        }

        return predicates;
    }

    @Override
    public AdminBorrowingDetailsDTO getAdminBorrowingDetails(Long borrowingId) {
        log.info("Fetching admin borrowing details for borrowing ID: {}", borrowingId);

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        return mapToAdminBorrowingDetailsDTO(borrowing);
    }

    @Override
    @Transactional
    public AdminBorrowingDetailsDTO updateBorrowingStatus(Long borrowingId, BorrowingStatusUpdateDTO statusUpdateDTO) {
        log.info("Updating borrowing status for borrowing ID: {} to status: {}",
                borrowingId, statusUpdateDTO.getStatus());

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        // Validate status transition
        validateStatusTransition(borrowing.getStatus(), statusUpdateDTO.getStatus());

        borrowing.setStatus(statusUpdateDTO.getStatus());

        // Add admin notes if provided
//        if (statusUpdateDTO.getAdminNotes() != null && !statusUpdateDTO.getAdminNotes().isEmpty()) {
//            // Assuming there's an adminNotes field in Borrowing entity
//            borrowing.setAdminNotes(statusUpdateDTO.getAdminNotes());
//        }

        // Handle specific status transitions
        if (statusUpdateDTO.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            borrowing.setReturnDate(LocalDateTime.now());
            handleItemReturn(borrowing);
        } else if (statusUpdateDTO.getStatus() == Borrowing.BorrowingStatus.OVERDUE) {
            calculateAndSetFine(borrowing);
        }

        // TODO: Send notification to user if notifyUser is true
        if (statusUpdateDTO.getNotifyUser() != null && statusUpdateDTO.getNotifyUser()) {
            sendStatusUpdateNotification(borrowing, statusUpdateDTO.getStatus());
        }

        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Successfully updated borrowing status for ID: {}", borrowingId);

        return mapToAdminBorrowingDetailsDTO(updatedBorrowing);
    }

//    @Override
//    @Transactional
//    public AdminBorrowingDetailsDTO extendBorrowingDueDate(Long borrowingId, BorrowingExtensionDTO extensionDTO) {
//        log.info("Extending due date for borrowing ID: {}", borrowingId);
//
//        Borrowing borrowing = borrowingRepository.findById(borrowingId)
//                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));
//
//        // Validate extension eligibility
//        validateExtensionEligibility(borrowing);
//
//        // Calculate new due date
//        LocalDateTime newDueDate;
//        if (extensionDTO.getNewDueDate() != null) {
//            newDueDate = extensionDTO.getNewDueDate();
//
//            // Validate that new due date is after current due date
//            if (newDueDate.isBefore(borrowing.getDueDate())) {
//                throw new InvalidOperationException("New due date cannot be before current due date");
//            }
//        } else if (extensionDTO.getExtensionDays() != null) {
//            newDueDate = borrowing.getDueDate().plusDays(extensionDTO.getExtensionDays());
//        } else {
//            // If neither is provided, use default extension days
//            newDueDate = borrowing.getDueDate().plusDays(DEFAULT_EXTENSION_DAYS);
//        }
//
//        // Update the borrowing with new due date
//        borrowing.setDueDate(newDueDate);
//
//        // Increment extension count
//        Integer currentExtensions = borrowing.getExtensionCount() != null ? borrowing.getExtensionCount() : 0;
//        borrowing.setExtensionCount(currentExtensions + 1);
//
//        // Set extension reason if provided
//        if (extensionDTO.getExtensionReason() != null && !extensionDTO.getExtensionReason().isEmpty()) {
//            borrowing.setExtensionReason(extensionDTO.getExtensionReason());
//        }
//
//        // Set admin notes if provided
//        if (extensionDTO.getAdminNotes() != null && !extensionDTO.getAdminNotes().isEmpty()) {
//            borrowing.setAdminNotes(extensionDTO.getAdminNotes());
//        }
//
//        // If borrowing was overdue, recalculate fine or reset status if appropriate
//        if (borrowing.getStatus() == Borrowing.BorrowingStatus.OVERDUE) {
//            borrowing.setStatus(Borrowing.BorrowingStatus.BORROWED);
//            // Reset fine amount if it was set due to overdue
//            borrowing.setFineAmount(BigDecimal.ZERO);
//        }
//
//        // TODO: Send notification to user if notifyUser is true
//        if (extensionDTO.getNotifyUser() != null && extensionDTO.getNotifyUser()) {
//            sendDueDateExtensionNotification(borrowing, newDueDate);
//        }
//
//        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
//        log.info("Successfully extended due date for borrowing ID: {} to {}", borrowingId, newDueDate);
//
//        return mapToAdminBorrowingDetailsDTO(updatedBorrowing);
//    }

    @Override
    @Transactional
    public AdminBorrowingDetailsDTO processBookReturn(Long borrowingId) {
        log.info("Processing book return for borrowing ID: {}", borrowingId);

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            throw new InvalidOperationException("Borrowing already returned");
        }

        // Handle item return
        handleItemReturn(borrowing);

        // Calculate fine if overdue
        calculateAndSetFine(borrowing);

        // Set return date and status
        borrowing.setReturnDate(LocalDateTime.now());
        borrowing.setStatus(Borrowing.BorrowingStatus.RETURNED);

        Borrowing returnedBorrowing = borrowingRepository.save(borrowing);
        log.info("Successfully processed return for borrowing ID: {}, fine amount: {}",
                borrowingId, borrowing.getFineAmount());

        return mapToAdminBorrowingDetailsDTO(returnedBorrowing);
    }

    @Override
    @Transactional
    public AdminBorrowingDetailsDTO adjustFineAmount(Long borrowingId, BigDecimal fineAmount) {
        log.info("Adjusting fine amount for borrowing ID: {} to {}", borrowingId, fineAmount);

        if (fineAmount == null || fineAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Fine amount cannot be negative");
        }

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        borrowing.setFineAmount(fineAmount);

        // If fine is set to zero, also set fineWaived to true if it was previously non-zero
        if (fineAmount.compareTo(BigDecimal.ZERO) == 0 &&
                borrowing.getFineAmount() != null &&
                borrowing.getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
//            borrowing.setFineWaived(true);
        }

        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Successfully adjusted fine amount for borrowing ID: {}", borrowingId);

        return mapToAdminBorrowingDetailsDTO(updatedBorrowing);
    }

    @Override
    @Transactional
    public AdminBorrowingDetailsDTO waiveFine(Long borrowingId) {
        log.info("Waiving fine for borrowing ID: {}", borrowingId);

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        if (borrowing.getFineAmount() == null || borrowing.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("No fine to waive for this borrowing");
        }

       // borrowing.setFineWaived(true);
        borrowing.setFineAmount(BigDecimal.ZERO);

        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
        log.info("Successfully waived fine for borrowing ID: {}", borrowingId);

        return mapToAdminBorrowingDetailsDTO(updatedBorrowing);
    }

    @Override
    public Page<AdminBorrowingDetailsDTO> getOverdueBorrowings(Pageable pageable) {
        log.info("Fetching overdue borrowings");

        Page<Borrowing> overdueBorrowings = borrowingRepository.findByStatusOrDueDateBefore(
                Borrowing.BorrowingStatus.OVERDUE,
                LocalDateTime.now(),
                pageable);

        List<AdminBorrowingDetailsDTO> borrowingDetailsDTOs = overdueBorrowings.stream()
                .map(this::mapToAdminBorrowingDetailsDTO)
                .toList();

        return new PageImpl<>(borrowingDetailsDTOs, pageable, overdueBorrowings.getTotalElements());
    }

    @Override
    public BorrowingStatisticsDTO getBorrowingStatistics() {
        log.info("Generating borrowing statistics");

        BorrowingStatisticsDTO statistics = new BorrowingStatisticsDTO();

        // Total borrowings
        long totalBorrowings = borrowingRepository.count();
        statistics.setTotalBorrowings(totalBorrowings);

        // Active borrowings
        long activeBorrowings = borrowingRepository.countByStatus(Borrowing.BorrowingStatus.BORROWED);
        statistics.setTotalActiveBorrowings(activeBorrowings);

        // Overdue borrowings
        long overdueBorrowings = borrowingRepository.countByStatus(Borrowing.BorrowingStatus.OVERDUE);

        // Also include borrowings that are past due date but not marked as overdue yet
        long pastDueDateCount = borrowingRepository.countByStatusAndDueDateBefore(
                Borrowing.BorrowingStatus.BORROWED, LocalDateTime.now());

        statistics.setTotalOverdueBorrowings(overdueBorrowings + pastDueDateCount);

        // Returned borrowings
        long returnedBorrowings = borrowingRepository.countByStatus(Borrowing.BorrowingStatus.RETURNED);
        statistics.setTotalReturnedBorrowings(returnedBorrowings);

        // Total fines collected/pending
//        BigDecimal totalFines = borrowingRepository.sumFineAmounts();
//        statistics.setTotalFinesCollected(totalFines != null ? totalFines : BigDecimal.ZERO);

        // Pending fines (borrowings with fines that have not been returned)
//        BigDecimal pendingFines = borrowingRepository.sumPendingFineAmounts();
//        statistics.setPendingFinesAmount(pendingFines != null ? pendingFines : BigDecimal.ZERO);

        // Most borrowed books
//        List<Object[]> mostBorrowedBooks = borrowingRepository.findMostBorrowedBooks(10);
//        Map<String, Long> topBooks = new HashMap<>();
//        for (Object[] result : mostBorrowedBooks) {
//            String bookTitle = (String) result[0];
//            Long count = (Long) result[1];
//            topBooks.put(bookTitle, count);
//        }
//        statistics.setMostBorrowedBooks(topBooks);

        // Most active users
//        List<Object[]> mostActiveUsers = borrowingRepository.findMostActiveUsers(10);
//        Map<String, Long> topUsers = new HashMap<>();
//        for (Object[] result : mostActiveUsers) {
//            String username = (String) result[0];
//            Long count = (Long) result[1];
//            topUsers.put(username, count);
//        }
//        statistics.setMostActiveUsers(topUsers);

        // Average borrowing duration
//        Double avgDuration = borrowingRepository.calculateAverageBorrowingDuration();
//        statistics.setAverageBorrowingDurationDays(avgDuration != null ? avgDuration : 0.0);

        return statistics;
    }

    @Override
    @Transactional
    public void deleteBorrowingByAdmin(Long borrowingId) {
        log.info("Admin deleting borrowing with ID: {}", borrowingId);

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with ID: " + borrowingId));

        // If it's an active borrowing, return the item to inventory
        if (borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED ||
                borrowing.getStatus() == Borrowing.BorrowingStatus.OVERDUE) {
            handleItemReturn(borrowing);
        }

        borrowingRepository.delete(borrowing);
        log.info("Successfully deleted borrowing with ID: {}", borrowingId);
    }

    // Helper methods

    private AdminBorrowingDetailsDTO mapToAdminBorrowingDetailsDTO(Borrowing borrowing) {
        AdminBorrowingDetailsDTO dto = adminBorrowingMapper.toAdminDTO(borrowing);

        // Calculate additional fields
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.RETURNED &&
                borrowing.getDueDate().isBefore(LocalDateTime.now())) {
            long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
            dto.setDaysOverdue(daysOverdue);
        }

        // Set renewable flag (can be renewed if not returned, not overdue, and less than max extensions)
//        Integer extensionCount = borrowing.getExtensionCount() != null ? borrowing.getExtensionCount() : 0;
//        boolean isRenewable = borrowing.getStatus() != Borrowing.BorrowingStatus.RETURNED &&
//                !borrowing.getDueDate().isBefore(LocalDateTime.now()) &&
//                extensionCount < MAX_EXTENSION_COUNT;
//        dto.setIsRenewable(isRenewable);

        return dto;
    }

    private void validateStatusTransition(Borrowing.BorrowingStatus currentStatus, Borrowing.BorrowingStatus newStatus) {
        if (currentStatus == Borrowing.BorrowingStatus.RETURNED && newStatus != Borrowing.BorrowingStatus.RETURNED) {
            throw new InvalidOperationException("Cannot change status of already returned borrowing");
        }

        if (currentStatus == newStatus) {
            throw new InvalidOperationException("Borrowing is already in " + newStatus + " status");
        }
    }

//    private void validateExtensionEligibility(Borrowing borrowing) {
//        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
//            throw new InvalidOperationException("Cannot extend due date of returned borrowing");
//        }
//
//        Integer extensionCount = borrowing.getExtensionCount() != null ? borrowing.getExtensionCount() : 0;
//        if (extensionCount >= MAX_EXTENSION_COUNT) {
//            throw new InvalidOperationException("Borrowing has reached maximum number of extensions: " + MAX_EXTENSION_COUNT);
//        }
//    }

    private void sendStatusUpdateNotification(Borrowing borrowing, Borrowing.BorrowingStatus newStatus) {
        // TODO: Implement notification service integration
        log.info("Notification would be sent to user: {} for borrowing ID: {} status change to: {}",
                borrowing.getUser().getUsername(), borrowing.getId(), newStatus);
    }

    private void sendDueDateExtensionNotification(Borrowing borrowing, LocalDateTime newDueDate) {
        // TODO: Implement notification service integration
        log.info("Notification would be sent to user: {} for borrowing ID: {} due date extension to: {}",
                borrowing.getUser().getUsername(), borrowing.getId(), newDueDate);
    }

    // Reuse existing methods from BorrowingServiceImpl
    private void handleItemReturn(Borrowing borrowing) {
        if (borrowing.getBook() != null) {
            Book book = borrowing.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        } else if (borrowing.getJournal() != null) {
            Journal journal = borrowing.getJournal();
            journal.setAvailableCopies(journal.getAvailableCopies() + 1);
            journalRepository.save(journal);
        }
    }

    private void calculateAndSetFine(Borrowing borrowing) {
        if (LocalDateTime.now().isAfter(borrowing.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
            BigDecimal daysOverdueBigDecimal = BigDecimal.valueOf(daysOverdue);
            BigDecimal fineAmount = daysOverdueBigDecimal.multiply(FINE_RATE_PER_DAY);
            borrowing.setFineAmount(fineAmount);
            if (borrowing.getStatus() != Borrowing.BorrowingStatus.OVERDUE) {
                borrowing.setStatus(Borrowing.BorrowingStatus.OVERDUE);
            }
        }
    }
}
