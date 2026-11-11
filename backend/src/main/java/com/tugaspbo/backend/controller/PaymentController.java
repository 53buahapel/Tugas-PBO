package com.tugaspbo.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugaspbo.backend.dto.request.CashPaymentRequest;
import com.tugaspbo.backend.dto.request.EWalletPaymentRequest;
import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.dto.response.PaymentResponse;
import com.tugaspbo.backend.security.UserPrincipal;
import com.tugaspbo.backend.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders/{orderId}/payments")
@Tag(name = "Payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/cash")
	@Operation(summary = "Pay an order with cash (customer only)")
	public ApiResponse<PaymentResponse> payCash(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody CashPaymentRequest request) {
		return ApiResponse.ok("Payment successful",
				paymentService.payCash(orderId, request, principal));
	}

	@PostMapping("/ewallet")
	@Operation(summary = "Pay an order with an e-wallet simulation (customer only)")
	public ApiResponse<PaymentResponse> payEWallet(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody EWalletPaymentRequest request) {
		return ApiResponse.ok("Payment successful",
				paymentService.payEWallet(orderId, request, principal));
	}

	@GetMapping
	@Operation(summary = "Get payment information for an order")
	public ApiResponse<PaymentResponse> getPayment(@PathVariable String orderId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ApiResponse.ok("Payment retrieved", paymentService.getPayment(orderId, principal));
	}
}
