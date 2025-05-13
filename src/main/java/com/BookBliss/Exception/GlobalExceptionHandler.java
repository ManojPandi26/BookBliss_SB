package com.BookBliss.Exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the LibraryHub application.
 * Handles various exceptions and provides standardized error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException.
     *
     * @param ex The exception
     * @return 404 Not Found response with error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
    )
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex The exception containing validation errors
     * @return 400 Bad Request response with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Map.class, example = "{\"username\":\"Username is required\",\"email\":\"Must be a valid email address\"}"))
    )
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles UserRegistrationException during user registration.
     *
     * @param ex The exception
     * @return 400 Bad Request response with error details
     */
    @ExceptionHandler(UserRegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "User registration failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleUserRegistrationException(UserRegistrationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles PasswordResetException during password reset operations.
     *
     * @param ex The exception
     * @return 400 Bad Request response with error details
     */
    @ExceptionHandler(PasswordResetException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Password reset failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handlePasswordResetException(PasswordResetException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ResourceNotFoundException when requested resources are not found.
     *
     * @param ex The exception
     * @return 404 Not Found response with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DuplicateResourceException when attempting to create duplicate resources.
     *
     * @param ex The exception
     * @return 409 Conflict response with error details
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ApiResponse(
            responseCode = "409",
            description = "Resource already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles InvalidOperationException when an operation cannot be performed.
     *
     * @param ex The exception
     * @return 400 Bad Request response with error details
     */
    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Invalid operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(InvalidOperationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Dropbox API exceptions.
     *
     * @param e The exception
     * @return 401 Unauthorized or 500 Internal Server Error response depending on exception type
     */
    @ExceptionHandler(DbxException.class)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid Dropbox credentials",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Dropbox operation failed",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
            )
    })
    public ResponseEntity<String> handleDropboxException(DbxException e) {
        if (e instanceof InvalidAccessTokenException) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Dropbox configuration. Please contact system administrator.");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to process file operation: " + e.getMessage());
    }

    /**
     * Handles BadCredentialsException when authentication fails.
     *
     * @param ex The exception
     * @return 401 Unauthorized response with error details
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Handles custom AuthenticationException for login failures.
     *
     * @param ex The exception
     * @return 401 Unauthorized response with error details
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex The exception
     * @return 500 Internal Server Error response with generic error message
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all Bookshelf exceptions.
     *
     * @param ex The exception
     * @return 500 Internal Server Error response with generic error message
     */
    @ExceptionHandler(BookshelfException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleBookshelfException(BookshelfException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all BookshelfItem exceptions.
     *
     * @param ex The exception
     * @return 500 Internal Server Error response with generic error message
     */
    @ExceptionHandler(BookshelfItemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleBookshelfItemException(BookshelfItemException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all Checkout exceptions.
     *
     * @param ex The exception
     * @return 500 Internal Server Error response with generic error message
     */
    @ExceptionHandler(CheckoutException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleCheckoutException(CheckoutException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}