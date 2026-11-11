package com.tugaspbo.backend.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

	private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	protected User() {
	}

	protected User(String id, String name, String email, String rawPassword) {
		this.id = id;
		this.name = name;
		this.email = email;
		setPassword(rawPassword);
	}

	public boolean login(String email, String rawPassword) {
		return this.email.equals(email) && PASSWORD_ENCODER.matches(rawPassword, this.password);
	}

	public abstract String getRole();

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String rawPassword) {
		this.password = PASSWORD_ENCODER.encode(rawPassword);
	}
}
