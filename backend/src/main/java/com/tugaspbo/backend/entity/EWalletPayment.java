package com.tugaspbo.backend.entity;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ewallet_payments")
public class EWalletPayment extends Payment {

	public static final Set<String> SUPPORTED_PROVIDERS = Set.of("DANA", "OVO", "GOPAY", "SHOPEEPAY");

	@Column(nullable = false)
	private String provider;

	@Column(nullable = false)
	private String accountNumber;

	protected EWalletPayment() {
	}

	public EWalletPayment(BigDecimal amount, String provider, String accountNumber) {
		super(amount);
		this.provider = provider;
		this.accountNumber = accountNumber;
	}

	@Override
	public boolean pay() {
		requirePending();
		boolean validProvider = provider != null
				&& SUPPORTED_PROVIDERS.contains(provider.trim().toUpperCase());
		boolean validAccount = accountNumber != null && accountNumber.matches("^\\d{10,15}$");
		if (!validProvider || !validAccount) {
			markFailed();
			return false;
		}
		markSuccess();
		return true;
	}

	@Override
	public String getMethodName() {
		return "E-WALLET";
	}

	public String getProvider() {
		return provider;
	}

	public String getAccountNumber() {
		return accountNumber;
	}
}
