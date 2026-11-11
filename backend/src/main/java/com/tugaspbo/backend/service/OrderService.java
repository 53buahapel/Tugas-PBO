package com.tugaspbo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.dto.request.CreateOrderRequest;
import com.tugaspbo.backend.dto.request.OrderItemRequest;
import com.tugaspbo.backend.dto.response.OrderResponse;
import com.tugaspbo.backend.entity.Customer;
import com.tugaspbo.backend.entity.MenuItem;
import com.tugaspbo.backend.entity.Order;
import com.tugaspbo.backend.enums.UserRole;
import com.tugaspbo.backend.exception.ResourceNotFoundException;
import com.tugaspbo.backend.exception.UnauthorizedException;
import com.tugaspbo.backend.repository.CustomerRepository;
import com.tugaspbo.backend.repository.MenuItemRepository;
import com.tugaspbo.backend.repository.OrderRepository;
import com.tugaspbo.backend.security.UserPrincipal;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final CustomerRepository customerRepository;
	private final MenuItemRepository menuItemRepository;

	public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
			MenuItemRepository menuItemRepository) {
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.menuItemRepository = menuItemRepository;
	}

	@Transactional
	public OrderResponse createOrder(UserPrincipal principal, CreateOrderRequest request) {
		Customer customer = customerRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
		Order order = customer.createOrder();
		for (OrderItemRequest itemRequest : request.items()) {
			order.addItem(findMenuItem(itemRequest.menuItemId()), itemRequest.quantity());
		}
		return OrderResponse.from(orderRepository.save(order));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> getOrders(UserPrincipal principal) {
		List<Order> orders = UserRole.ADMIN.name().equals(principal.getRole())
				? orderRepository.findAllByOrderByCreatedAtDesc()
				: orderRepository.findByCustomerIdOrderByCreatedAtDesc(principal.getId());
		return orders.stream().map(OrderResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(String orderId, UserPrincipal principal) {
		return OrderResponse.from(findOwnedOrder(orderId, principal));
	}

	@Transactional
	public OrderResponse addItem(String orderId, OrderItemRequest request, UserPrincipal principal) {
		Order order = findOwnedOrder(orderId, principal);
		order.addItem(findMenuItem(request.menuItemId()), request.quantity());
		return OrderResponse.from(order);
	}

	@Transactional
	public OrderResponse removeItem(String orderId, String menuItemId, UserPrincipal principal) {
		Order order = findOwnedOrder(orderId, principal);
		if (!order.removeItem(menuItemId)) {
			throw new ResourceNotFoundException(
					"Item " + menuItemId + " is not part of order " + orderId);
		}
		return OrderResponse.from(order);
	}

	@Transactional
	public OrderResponse cancelOrder(String orderId, UserPrincipal principal) {
		Order order = findOwnedOrder(orderId, principal);
		order.cancel();
		return OrderResponse.from(order);
	}

	Order findOwnedOrder(String orderId, UserPrincipal principal) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
		if (UserRole.CUSTOMER.name().equals(principal.getRole())
				&& !order.getCustomer().getId().equals(principal.getId())) {
			throw new UnauthorizedException("You can only access your own orders");
		}
		return order;
	}

	private MenuItem findMenuItem(String menuItemId) {
		return menuItemRepository.findByIdAndDeletedFalse(menuItemId)
				.orElseThrow(
						() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));
	}
}
