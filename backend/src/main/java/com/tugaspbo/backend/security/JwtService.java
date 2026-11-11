package com.tugaspbo.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tugaspbo.backend.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(User user) {
		Date now = new Date();
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("uid", user.getId())
				.claim("name", user.getName())
				.claim("role", user.getRole())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMs))
				.signWith(key)
				.compact();
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isValid(String token, String email) {
		Claims claims = parseClaims(token);
		return claims.getSubject().equals(email) && claims.getExpiration().after(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}
}
