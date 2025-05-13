//package com.LibraryHub.Entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.*;
//import org.hibernate.annotations.Cache;
//import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "roles",
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = "name")
//        },
//        indexes = {
//                @Index(name = "idx_role_name", columnList = "name")
//        })
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Role {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank(message = "Role name is required")
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 50)
//    private RoleName name;
//
//    @Size(max = 255, message = "Description cannot exceed 255 characters")
//    @Column(length = 255)
//    private String description;
//
//    @Builder.Default
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "role_permissions",
//            joinColumns = @JoinColumn(name = "role_id"),
//            inverseJoinColumns = @JoinColumn(name = "permission_id"),
//            indexes = {
//                    @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
//                    @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
//            }
//    )
//    private Set<Permission> permissions = new HashSet<>();
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;
//
//    @Column(name = "is_default", nullable = false)
//    private boolean isDefault = false;
//
//    @Column(name = "is_system", nullable = false)
//    private boolean isSystem = true;
//
//    public void addPermission(Permission permission) {
//        this.permissions.add(permission);
//    }
//
//    public void removePermission(Permission permission) {
//        this.permissions.remove(permission);
//    }
//
//    public boolean hasPermission(Permission permission) {
//        return this.permissions.contains(permission);
//    }
//
//    public enum RoleName {
//        ROLE_ADMIN,
//        ROLE_LIBRARIAN,
//        ROLE_MEMBER,
//        ROLE_PREMIUM_MEMBER,
//        ROLE_GUEST
//    }
//}