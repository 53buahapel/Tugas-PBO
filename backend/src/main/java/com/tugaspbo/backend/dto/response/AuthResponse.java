package com.tugaspbo.backend.dto.response;

public record AuthResponse(
		String token,
		String userId,
		String name,
		String email,
		String role) {
}
