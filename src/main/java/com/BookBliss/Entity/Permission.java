//package com.LibraryHub.Entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.*;
//import org.hibernate.annotations.Cache;
//import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.NaturalId;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "permissions",
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = "name")
//        },
//        indexes = {
//                @Index(name = "idx_permission_name", columnList = "name")
//        })
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Permission {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @NaturalId
//    @Column(nullable = false, length = 100)
//    private PermissionName name;
//
//    @NotBlank(message = "Permission description is required")
//    @Size(max = 200, message = "Description cannot exceed 200 characters")
//    @Column(nullable = false, length = 200)
//    private String description;
//
//    @Column(nullable = false, length = 50)
//    private String category;
//
//    @ManyToMany(mappedBy = "permissions")
//    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//    @Builder.Default
//    private Set<Role> roles = new HashSet<>();
//
//    // Enum for permission names with description comments
//    public enum PermissionName {
//        // Book permissions
//        BOOK_CREATE,           // Can create new books
//        BOOK_READ,             // Can view book details
//        BOOK_UPDATE,           // Can update book information
//        BOOK_DELETE,           // Can delete books
//        BOOK_BORROW,           // Can borrow books
//        BOOK_RETURN,           // Can return books
//        BOOK_RENEW,            // Can renew borrowed books
//        BOOK_RESERVE,          // Can reserve books
//
//        // User permissions
//        USER_CREATE,           // Can create new users
//        USER_READ,             // Can view user details
//        USER_UPDATE,           // Can update user information
//        USER_DELETE,           // Can delete users
//        USER_MANAGE,           // Can manage user accounts (suspend, activate)
//
//        // Role/Permission management
//        ROLE_CREATE,           // Can create new roles
//        ROLE_READ,             // Can view role details
//        ROLE_UPDATE,           // Can update role information
//        ROLE_DELETE,           // Can delete roles
//        PERMISSION_ASSIGN,     // Can assign permissions to roles
//
//        // Library management
//        LIBRARY_MANAGE,        // Can manage library settings
//        CATEGORY_MANAGE,       // Can manage book categories
//        AUTHOR_MANAGE,         // Can manage authors
//        PUBLISHER_MANAGE,      // Can manage publishers
//
//        // Reports and statistics
//        REPORT_VIEW,           // Can view reports
//        REPORT_GENERATE,       // Can generate reports
//        STATISTICS_VIEW,       // Can view library statistics
//
//        // Advanced features
//        FINE_MANAGE,           // Can manage fines
//        PAYMENT_PROCESS,       // Can process payments
//        EVENT_MANAGE,          // Can manage library events
//        NOTIFICATION_MANAGE,   // Can manage notifications
//
//        // System administration
//        SYSTEM_SETTINGS,       // Can access system settings
//        SYSTEM_BACKUP,         // Can perform system backup
//        SYSTEM_RESTORE,        // Can restore system from backup
//        LOG_VIEW,              // Can view system logs
//        AUDIT_VIEW             // Can view audit logs
//    }
//}