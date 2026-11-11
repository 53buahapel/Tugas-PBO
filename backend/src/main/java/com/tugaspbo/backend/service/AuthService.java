package com.tugaspbo.backend.service;

import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.dto.request.AdminRegisterRequest;
import com.tugaspbo.backend.dto.request.LoginRequest;
import com.tugaspbo.backend.dto.request.RegisterRequest;
import com.tugaspbo.backend.dto.response.AuthResponse;
import com.tugaspbo.backend.entity.Admin;
import com.tugaspbo.backend.entity.Customer;
import com.tugaspbo.backend.entity.Menu;
import com.tugaspbo.backend.entity.User;
import com.tugaspbo.backend.exception.ConflictException;
import com.tugaspbo.backend.exception.ResourceNotFoundException;
import com.tugaspbo.backend.repository.AdminRepository;
import com.tugaspbo.backend.repository.CustomerRepository;
import com.tugaspbo.backend.repository.MenuRepository;
import com.tugaspbo.backend.repository.UserRepository;
import com.tugaspbo.backend.security.JwtService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final AdminRepository adminRepository;
	private final MenuRepository menuRepository;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, CustomerRepository customerRepository,
			AdminRepository adminRepository, MenuRepository menuRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
		this.adminRepository = adminRepository;
		this.menuRepository = menuRepository;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email is already registered: " + request.email());
		}
		Customer customer = new Customer(UUID.randomUUID().toString(), request.name(),
				request.email(), request.password(), request.phone());
		customerRepository.save(customer);
		return toAuthResponse(customer);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.filter(u -> u.login(request.email(), request.password()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return toAuthResponse(user);
	}

	@Transactional
	public AuthResponse registerAdmin(AdminRegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email is already registered: " + request.email());
		}
		Menu menu = menuRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new ResourceNotFoundException("Menu is not initialized"));
		Admin admin = new Admin(UUID.randomUUID().toString(), request.name(), request.email(),
				request.password(), menu);
		adminRepository.save(admin);
		return toAuthResponse(admin);
	}

	private AuthResponse toAuthResponse(User user) {
		return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getName(),
				user.getEmail(), user.getRole());
	}
}
