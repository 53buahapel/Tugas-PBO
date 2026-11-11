package com.tugaspbo.backend.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.tugaspbo.backend.enums.MenuCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItem {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private boolean available;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MenuCategory category;

	@Column(nullable = false)
	private boolean deleted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id")
	private Menu menu;

	protected MenuItem() {
	}

	public MenuItem(String name, BigDecimal price, MenuCategory category) {
		this("MENU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
				name, price, category);
	}

	public MenuItem(String id, String name, BigDecimal price, MenuCategory category) {
		this.id = id;
		setName(name);
		updatePrice(price);
		this.category = category;
		this.available = true;
		this.deleted = false;
	}

	public void updatePrice(double newPrice) {
		updatePrice(BigDecimal.valueOf(newPrice));
	}

	public void updatePrice(BigDecimal newPrice) {
		if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price must be greater than zero");
		}
		this.price = newPrice;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public boolean isAvailable() {
		return available;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public MenuCategory getCategory() {
		return category;
	}

	public void setCategory(MenuCategory category) {
		this.category = category;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Menu getMenu() {
		return menu;
	}

	void setMenu(Menu menu) {
		this.menu = menu;
	}
}
