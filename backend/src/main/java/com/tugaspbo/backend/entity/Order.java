package com.tugaspbo.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.tugaspbo.backend.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {

	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
			OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
			OrderStatus.PAID, Set.of(OrderStatus.PROCESSING),
			OrderStatus.PROCESSING, Set.of(OrderStatus.COMPLETED));

	@Id
	@Column(name = "order_id", length = 36)
	private String orderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@Version
	private long version;

	protected Order() {
	}

	public Order(Customer customer) {
		this.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		this.customer = customer;
		this.status = OrderStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}

	public void addItem(MenuItem menuItem, int quantity) {
		requireStatus(OrderStatus.PENDING, "Items can only be added to a PENDING order");
		if (!menuItem.isAvailable() || menuItem.isDeleted()) {
			throw new IllegalArgumentException("Menu item is not available: " + menuItem.getName());
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
		items.stream()
				.filter(item -> item.getMenuItem().getId().equals(menuItem.getId()))
				.findFirst()
				.ifPresentOrElse(
						item -> item.increaseQuantity(quantity),
						() -> items.add(new OrderItem(this, menuItem, quantity)));
		recalculateTotal();
	}

	public boolean removeItem(String menuItemId) {
		requireStatus(OrderStatus.PENDING, "Items can only be removed from a PENDING order");
		boolean removed = items.removeIf(item -> item.getMenuItem().getId().equals(menuItemId));
		if (removed) {
			recalculateTotal();
		}
		return removed;
	}

	public BigDecimal calculateTotal() {
		return items.stream()
				.map(OrderItem::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public boolean checkout(Payment payment) {
		requireStatus(OrderStatus.PENDING, "Only a PENDING order can be checked out");
		if (items.isEmpty()) {
			throw new IllegalStateException("Cannot checkout an empty order");
		}
		if (payment.getAmount().compareTo(calculateTotal()) != 0) {
			throw new IllegalArgumentException("Payment amount must equal the order total");
		}
		setPayment(payment);
		boolean success = payment.pay();
		if (success) {
			transitionTo(OrderStatus.PAID);
		}
		return success;
	}

	public void markProcessing() {
		transitionTo(OrderStatus.PROCESSING);
	}

	public void markCompleted() {
		transitionTo(OrderStatus.COMPLETED);
	}

	public void cancel() {
		transitionTo(OrderStatus.CANCELLED);
	}

	private void transitionTo(OrderStatus target) {
		Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(status, Set.of());
		if (!allowed.contains(target)) {
			throw new IllegalStateException(
					"Invalid order status transition: " + status + " -> " + target);
		}
		this.status = target;
	}

	private void requireStatus(OrderStatus expected, String message) {
		if (this.status != expected) {
			throw new IllegalStateException(message + " (current status: " + status + ")");
		}
	}

	private void recalculateTotal() {
		this.total = calculateTotal();
	}

	public String getOrderId() {
		return orderId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public List<OrderItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public OrderStatus getStatus() {
		return status;
	}

	public Payment getPayment() {
		return payment;
	}

	private void setPayment(Payment payment) {
		this.payment = payment;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public BigDecimal getTotal() {
		return total;
	}
}
