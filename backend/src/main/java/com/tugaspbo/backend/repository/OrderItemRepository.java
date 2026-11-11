package com.tugaspbo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

	boolean existsByMenuItemId(String menuItemId);
}
