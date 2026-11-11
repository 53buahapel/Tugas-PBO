package com.tugaspbo.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.dto.response.DashboardResponse;
import com.tugaspbo.backend.dto.response.OrderResponse;
import com.tugaspbo.backend.service.AdminService;
import com.tugaspbo.backend.service.OrderService;
import com.tugaspbo.backend.security.UserPrincipal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	private final AdminService adminService;
	private final OrderService orderService;

	public AdminController(AdminService adminService, OrderService orderService) {
		this.adminService = adminService;
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	@Operation(summary = "Get all orders (admin only)")
	public ApiResponse<List<OrderResponse>> getAllOrders(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Orders retrieved", orderService.getOrders(principal));
	}

	@PatchMapping("/orders/{orderId}/processing")
	@Operation(summary = "Move a PAID order to PROCESSING (admin only)")
	public ApiResponse<OrderResponse> markProcessing(@PathVariable String orderId) {
		return ApiResponse.ok("Order moved to PROCESSING", adminService.markProcessing(orderId));
	}

	@PatchMapping("/orders/{orderId}/complete")
	@Operation(summary = "Move a PROCESSING order to COMPLETED (admin only)")
	public ApiResponse<OrderResponse> markCompleted(@PathVariable String orderId) {
		return ApiResponse.ok("Order completed", adminService.markCompleted(orderId));
	}

	@GetMapping("/dashboard")
	@Operation(summary = "Get order and revenue statistics (admin only)")
	public ApiResponse<DashboardResponse> getDashboard() {
		return ApiResponse.ok("Dashboard statistics retrieved", adminService.getDashboard());
	}
}
