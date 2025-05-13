package com.BookBliss.Controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.BookBliss.DTO.Auth.PasswordUpdateDto;
import com.BookBliss.DTO.UserProfile.UserDetailsDTO;
import com.BookBliss.DTO.UserProfile.UserProfileUpdateDto;
import com.BookBliss.Service.User.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserServiceImpl userService;

	@GetMapping("/profile/{userId}")
    public ResponseEntity<UserDetailsDTO> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

	 @PutMapping("/profile/{userId}")
	    public ResponseEntity<UserDetailsDTO> updateUserProfile(
	            @PathVariable Long userId,
	            @Valid @RequestBody UserProfileUpdateDto updateDto) {
	        return ResponseEntity.ok(userService.updateUserProfile(userId, updateDto));
	    }

	@PutMapping("/profile/{userId}/change-password")
	public ResponseEntity<?> updatePassword(@PathVariable Long userId,
			@Valid @RequestBody PasswordUpdateDto passwordDto) {
		userService.updatePassword(userId, passwordDto);
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/profile/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserDetailsDTO> updateProfileImage(@PathVariable Long userId,
			@RequestParam("image") MultipartFile imageFile) {
		return ResponseEntity.ok(userService.updateUserProfileImage(userId, imageFile));
	}

	@DeleteMapping("/profile/{userId}/image")
	public ResponseEntity<Void> deleteProfileImage(@PathVariable Long userId) {
		userService.deleteUserProfileImage(userId);
		return ResponseEntity.ok().build();
	}

}