package com.tugaspbo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
