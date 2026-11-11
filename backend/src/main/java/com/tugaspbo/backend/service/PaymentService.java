package com.tugaspbo.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.dto.request.CashPaymentRequest;
import com.tugaspbo.backend.dto.request.EWalletPaymentRequest;
import com.tugaspbo.backend.dto.response.PaymentResponse;
import com.tugaspbo.backend.entity.CashPayment;
import com.tugaspbo.backend.entity.EWalletPayment;
import com.tugaspbo.backend.entity.Order;
import com.tugaspbo.backend.entity.Payment;
import com.tugaspbo.backend.enums.OrderStatus;
import com.tugaspbo.backend.exception.BadRequestException;
import com.tugaspbo.backend.exception.ConflictException;
import com.tugaspbo.backend.exception.ResourceNotFoundException;
import com.tugaspbo.backend.security.UserPrincipal;

@Service
public class PaymentService {

	private final OrderService orderService;

	public PaymentService(OrderService orderService) {
		this.orderService = orderService;
	}

	@Transactional
	public PaymentResponse payCash(String orderId, CashPaymentRequest request,
			UserPrincipal principal) {
		Order order = findPayableOrder(orderId, principal);
		Payment payment = new CashPayment(order.calculateTotal(), request.cashGiven());
		if (!order.checkout(payment)) {
			throw new BadRequestException(
					"Insufficient cash: " + request.cashGiven() + " is less than order total "
							+ order.calculateTotal());
		}
		return PaymentResponse.from(payment);
	}

	@Transactional
	public PaymentResponse payEWallet(String orderId, EWalletPaymentRequest request,
			UserPrincipal principal) {
		Order order = findPayableOrder(orderId, principal);
		Payment payment = new EWalletPayment(order.calculateTotal(), request.provider(),
				request.accountNumber());
		if (!order.checkout(payment)) {
			throw new BadRequestException("Invalid e-wallet provider or account number");
		}
		return PaymentResponse.from(payment);
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPayment(String orderId, UserPrincipal principal) {
		Order order = orderService.findOwnedOrder(orderId, principal);
		if (order.getPayment() == null) {
			throw new ResourceNotFoundException("No payment found for order " + orderId);
		}
		return PaymentResponse.from(order.getPayment());
	}

	private Order findPayableOrder(String orderId, UserPrincipal principal) {
		Order order = orderService.findOwnedOrder(orderId, principal);
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new ConflictException(
					"Order " + orderId + " cannot be paid (status: " + order.getStatus() + ")");
		}
		return order;
	}
}
