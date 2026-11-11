package com.tugaspbo.backend.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tugaspbo.backend.dto.response.ApiResponse;

import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({ BadRequestException.class, IllegalArgumentException.class })
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleForbidden(UnauthorizedException ex) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler({ ConflictException.class, IllegalStateException.class,
			OptimisticLockException.class, OptimisticLockingFailureException.class })
	public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
			MethodArgumentNotValidException ex) {
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField,
						e -> e.getDefaultMessage() == null ? "invalid" : e.getDefaultMessage(),
						(a, b) -> a));
		return ResponseEntity.badRequest()
				.body(ApiResponse.error("Validation failed", errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}

	private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(ApiResponse.error(message, null));
	}
}
