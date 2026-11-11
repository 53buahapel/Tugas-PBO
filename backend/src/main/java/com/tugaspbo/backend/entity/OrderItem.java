package com.tugaspbo.backend.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id", nullable = false)
	private MenuItem menuItem;

	@Column(nullable = false)
	private String itemName;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false)
	private int quantity;

	protected OrderItem() {
	}

	public OrderItem(Order order, MenuItem menuItem, int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
		this.id = UUID.randomUUID().toString();
		this.order = order;
		this.menuItem = menuItem;
		this.itemName = menuItem.getName();
		this.unitPrice = menuItem.getPrice();
		this.quantity = quantity;
	}

	public BigDecimal getSubtotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public void increaseQuantity(int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Quantity increment must be positive");
		}
		this.quantity += amount;
	}

	void setQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
		this.quantity = quantity;
	}

	public String getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public String getItemName() {
		return itemName;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}
}
