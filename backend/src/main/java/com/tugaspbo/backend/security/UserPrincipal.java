package com.tugaspbo.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tugaspbo.backend.entity.User;

public class UserPrincipal implements UserDetails {

	private final String id;
	private final String name;
	private final String email;
	private final String password;
	private final String role;

	public UserPrincipal(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.role = user.getRole();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRole() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}
}
