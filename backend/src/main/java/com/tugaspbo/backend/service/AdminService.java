package com.tugaspbo.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.dto.response.DashboardResponse;
import com.tugaspbo.backend.dto.response.OrderResponse;
import com.tugaspbo.backend.entity.Order;
import com.tugaspbo.backend.enums.OrderStatus;
import com.tugaspbo.backend.enums.PaymentStatus;
import com.tugaspbo.backend.exception.ResourceNotFoundException;
import com.tugaspbo.backend.repository.OrderRepository;
import com.tugaspbo.backend.repository.PaymentRepository;

@Service
public class AdminService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;

	public AdminService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
	}

	@Transactional
	public OrderResponse markProcessing(String orderId) {
		Order order = findOrder(orderId);
		order.markProcessing();
		return OrderResponse.from(order);
	}

	@Transactional
	public OrderResponse markCompleted(String orderId) {
		Order order = findOrder(orderId);
		order.markCompleted();
		return OrderResponse.from(order);
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard() {
		return new DashboardResponse(
				orderRepository.count(),
				orderRepository.countByStatus(OrderStatus.PENDING),
				orderRepository.countByStatus(OrderStatus.PAID),
				orderRepository.countByStatus(OrderStatus.PROCESSING),
				orderRepository.countByStatus(OrderStatus.COMPLETED),
				paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS));
	}

	private Order findOrder(String orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
	}
}
