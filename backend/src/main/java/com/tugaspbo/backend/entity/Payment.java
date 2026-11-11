package com.tugaspbo.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tugaspbo.backend.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Payment {

	@Id
	@Column(length = 36)
	private String paymentId;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column
	private LocalDateTime paidAt;

	protected Payment() {
	}

	protected Payment(BigDecimal amount) {
		this.paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		this.amount = amount;
		this.status = PaymentStatus.PENDING;
	}

	public abstract boolean pay();

	public abstract String getMethodName();

	protected void markSuccess() {
		this.status = PaymentStatus.SUCCESS;
		this.paidAt = LocalDateTime.now();
	}

	protected void markFailed() {
		this.status = PaymentStatus.FAILED;
	}

	protected void requirePending() {
		if (this.status != PaymentStatus.PENDING) {
			throw new IllegalStateException("Payment has already been processed");
		}
	}

	public String getPaymentId() {
		return paymentId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}
}
