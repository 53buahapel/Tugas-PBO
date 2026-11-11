package com.tugaspbo.backend.entity;

import com.tugaspbo.backend.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id")
	private Menu menu;

	protected Admin() {
	}

	public Admin(String id, String name, String email, String rawPassword, Menu menu) {
		super(id, name, email, rawPassword);
		this.menu = menu;
	}

	public void addMenuItem(MenuItem item) {
		menu.addItem(item);
	}

	public void updateMenuPrice(String itemId, double newPrice) {
		menu.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemId))
				.updatePrice(newPrice);
	}

	public void setMenuAvailability(String itemId, boolean available) {
		menu.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemId))
				.setAvailable(available);
	}

	public void removeMenuItem(String itemId) {
		if (!menu.removeItem(itemId)) {
			throw new IllegalArgumentException("Menu item not found: " + itemId);
		}
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	@Override
	public String getRole() {
		return UserRole.ADMIN.name();
	}
}
