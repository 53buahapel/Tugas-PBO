package com.tugaspbo.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugaspbo.backend.dto.request.CreateOrderRequest;
import com.tugaspbo.backend.dto.request.OrderItemRequest;
import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.dto.response.OrderResponse;
import com.tugaspbo.backend.security.UserPrincipal;
import com.tugaspbo.backend.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@Operation(summary = "Create a new order (customer only)")
	public ResponseEntity<ApiResponse<OrderResponse>> create(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody CreateOrderRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Order created successfully",
						orderService.createOrder(principal, request)));
	}

	@GetMapping
	@Operation(summary = "Get my orders (admin sees all)")
	public ApiResponse<List<OrderResponse>> getOrders(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Orders retrieved", orderService.getOrders(principal));
	}

	@GetMapping("/{orderId}")
	@Operation(summary = "Get order details")
	public ApiResponse<OrderResponse> getOrder(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Order retrieved", orderService.getOrder(orderId, principal));
	}

	@GetMapping("/{orderId}/total")
	@Operation(summary = "Get order total")
	public ApiResponse<OrderResponse> getTotal(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Order total calculated", orderService.getOrder(orderId, principal));
	}

	@PostMapping("/{orderId}/items")
	@Operation(summary = "Add an item to a PENDING order")
	public ApiResponse<OrderResponse> addItem(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody OrderItemRequest request) {
		return ApiResponse.ok("Item added to order",
				orderService.addItem(orderId, request, principal));
	}

	@DeleteMapping("/{orderId}/items/{menuItemId}")
	@Operation(summary = "Remove an item from a PENDING order")
	public ApiResponse<OrderResponse> removeItem(@PathVariable String orderId,
			@PathVariable String menuItemId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Item removed from order",
				orderService.removeItem(orderId, menuItemId, principal));
	}

	@PostMapping("/{orderId}/cancel")
	@Operation(summary = "Cancel an eligible order")
	public ApiResponse<OrderResponse> cancel(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Order cancelled", orderService.cancelOrder(orderId, principal));
	}
}
