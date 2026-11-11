package com.tugaspbo.backend.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tugaspbo.backend.entity.Payment;
import com.tugaspbo.backend.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, String> {

	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
	BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
}
