package com.BookBliss.Exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "Standard error response object")
public  class ErrorResponse {
	@Schema(description = "HTTP status code", example = "400")
	private final int status;

	@Schema(description = "Error message", example = "Resource not found")
	private final String message;

	@Schema(description = "Timestamp when the error occurred", example = "2023-03-01T15:30:45.123")
	private final LocalDateTime timestamp;

	public ErrorResponse(int status, String message, LocalDateTime timestamp) {
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
	}


}
