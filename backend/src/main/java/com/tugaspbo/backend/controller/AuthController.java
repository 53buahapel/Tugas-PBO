package com.tugaspbo.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugaspbo.backend.dto.request.AdminRegisterRequest;
import com.tugaspbo.backend.dto.request.LoginRequest;
import com.tugaspbo.backend.dto.request.RegisterRequest;
import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.dto.response.AuthResponse;
import com.tugaspbo.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@Operation(summary = "Register a new customer")
	public ResponseEntity<ApiResponse<AuthResponse>> register(
			@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Customer registered successfully",
						authService.register(request)));
	}

	@PostMapping("/login")
	@Operation(summary = "Login and obtain a JWT")
	public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("Login successful", authService.login(request));
	}

	@PostMapping("/admin/register")
	@Operation(summary = "Register a new admin (admin only)")
	public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(
			@Valid @RequestBody AdminRegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Admin registered successfully",
						authService.registerAdmin(request)));
	}
}
