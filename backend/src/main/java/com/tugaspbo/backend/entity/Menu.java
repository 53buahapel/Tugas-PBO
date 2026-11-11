package com.tugaspbo.backend.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "menus")
public class Menu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "menu", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<MenuItem> items = new ArrayList<>();

	protected Menu() {
	}

	public Menu(String name) {
		this.name = name;
	}

	public void addItem(MenuItem item) {
		item.setMenu(this);
		items.add(item);
	}

	public boolean removeItem(String itemId) {
		return items.removeIf(item -> item.getId().equals(itemId));
	}

	public Optional<MenuItem> findById(String itemId) {
		return items.stream()
				.filter(item -> item.getId().equals(itemId) && !item.isDeleted())
				.findFirst();
	}

	public List<MenuItem> getAvailableItems() {
		return items.stream()
				.filter(item -> item.isAvailable() && !item.isDeleted())
				.toList();
	}

	public void display() {
		System.out.println("=== " + name + " ===");
		items.stream()
				.filter(item -> !item.isDeleted())
				.forEach(item -> System.out.printf("%s | %-20s | %-7s | Rp%.0f | %s%n",
						item.getId(), item.getName(), item.getCategory(),
						item.getPrice(), item.isAvailable() ? "available" : "unavailable"));
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<MenuItem> getItems() {
		return Collections.unmodifiableList(items);
	}
}
