package com.tugaspbo.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cash_payments")
public class CashPayment extends Payment {

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal cashGiven;

	@Column(precision = 12, scale = 2)
	private BigDecimal change;

	protected CashPayment() {
	}

	public CashPayment(BigDecimal amount, BigDecimal cashGiven) {
		super(amount);
		this.cashGiven = cashGiven;
	}

	@Override
	public boolean pay() {
		requirePending();
		if (cashGiven == null || cashGiven.compareTo(getAmount()) < 0) {
			markFailed();
			return false;
		}
		this.change = cashGiven.subtract(getAmount());
		markSuccess();
		return true;
	}

	@Override
	public String getMethodName() {
		return "CASH";
	}

	public BigDecimal getCashGiven() {
		return cashGiven;
	}

	public BigDecimal getChange() {
		return change;
	}
}
