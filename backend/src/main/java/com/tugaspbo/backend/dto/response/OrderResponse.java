package com.tugaspbo.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tugaspbo.backend.entity.Order;
import com.tugaspbo.backend.enums.OrderStatus;

public record OrderResponse(
		String orderId,
		String customerId,
		String customerName,
		OrderStatus status,
		BigDecimal total,
		List<OrderItemResponse> items,
		PaymentResponse payment,
		LocalDateTime createdAt) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getOrderId(),
				order.getCustomer().getId(),
				order.getCustomer().getName(),
				order.getStatus(),
				order.getTotal(),
				order.getItems().stream().map(OrderItemResponse::from).toList(),
				order.getPayment() == null ? null : PaymentResponse.from(order.getPayment()),
				order.getCreatedAt());
	}
}
