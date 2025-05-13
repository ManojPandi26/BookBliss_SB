package com.BookBliss.Mapper;



import com.BookBliss.DTO.Admin.UserManagement.AdminUserDetailsDTO;
import org.springframework.stereotype.Component;

import com.BookBliss.DTO.UserProfile.UserDetailsDTO;
import com.BookBliss.DTO.Auth.UserDto;
import com.BookBliss.Entity.User;

@Component
public class UserMapper {
    
   public UserDto toDto(User user) {
	   UserDto userdto=new UserDto();
	   userdto.setId(user.getId());
	   userdto.setEmail(user.getEmail());
	   userdto.setUsername(user.getUsername());
	   userdto.setRole(user.getRole());
	   return userdto;
   }
    public User toEntity(UserDto userDto) {
    	User user=new User();
    	
    	user.setEmail(userDto.getEmail());
    	
    	user.setId(userDto.getId());
    	
    	user.setUsername(userDto.getUsername());
    	user.setRole(userDto.getRole());
    	return user;
    }
    public UserDetailsDTO toProfileDto(User user) {
        return UserDetailsDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
				.isActive(user.isActive())
				.emailVerified(user.isEmailVerified())
				.updatedAt(user.getUpdatedAt())
            .phoneNumber(user.getPhoneNumber())
            .address(user.getAddress())
            .role(user.getRole().name())
            .createdAt(user.getCreatedAt())
            .LastLogin(user.getLastLogin())
            .profileImageUrl(user.getProfileImageUrl())
            .build();
    }

    public AdminUserDetailsDTO toAdminDto(User user) {
	   return AdminUserDetailsDTO.builder()
			   .username(user.getUsername())
			   .id(user.getId())
			   .role(user.getRole())
			   .fullName(user.getFullName())
			   .email(user.getEmail())
			   .profileImageUrl(user.getProfileImageUrl())
			   .address(user.getAddress())
			   .createdAt(user.getCreatedAt())
			   .emailVerified(user.isEmailVerified())
			   .lastLogin(user.getLastLogin())
			   .phoneNumber(user.getPhoneNumber())
			   .active(user.isActive())
			   .build();
    }
}
