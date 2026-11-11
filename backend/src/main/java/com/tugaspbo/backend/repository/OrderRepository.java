package com.tugaspbo.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.Order;
import com.tugaspbo.backend.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, String> {

	List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

	List<Order> findAllByOrderByCreatedAtDesc();

	long countByStatus(OrderStatus status);
}
