package com.tugaspbo.backend.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tugaspbo.backend.enums.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends User {

	@Column
	private String phone;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<Order> orderHistory = new ArrayList<>();

	protected Customer() {
	}

	public Customer(String id, String name, String email, String rawPassword, String phone) {
		super(id, name, email, rawPassword);
		this.phone = phone;
	}

	public Order createOrder() {
		return new Order(this);
	}

	public List<Order> getOrderHistory() {
		return Collections.unmodifiableList(orderHistory);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String getRole() {
		return UserRole.CUSTOMER.name();
	}
}
