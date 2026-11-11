package com.tugaspbo.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login")
						.permitAll()
						.requestMatchers("/api/auth/admin/register").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/menu", "/api/menu/**").permitAll()
						.requestMatchers("/api/menu/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/orders", "/api/orders/*/items",
								"/api/orders/*/cancel", "/api/orders/*/payments/*")
						.hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.DELETE, "/api/orders/*/items/*")
						.hasRole("CUSTOMER")
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
						.permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> writeError(
								response, HttpStatus.UNAUTHORIZED, "Authentication required"))
						.accessDeniedHandler((request, response, accessDeniedException) -> writeError(
								response, HttpStatus.FORBIDDEN,
								"You do not have permission to access this resource")))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static void writeError(HttpServletResponse response, HttpStatus status, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		OBJECT_MAPPER.writeValue(response.getWriter(), ApiResponse.error(message, null));
	}
}
