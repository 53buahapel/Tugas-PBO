package com.tugaspbo.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tugaspbo.backend.entity.CashPayment;
import com.tugaspbo.backend.entity.EWalletPayment;
import com.tugaspbo.backend.entity.Payment;
import com.tugaspbo.backend.enums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentResponse(
		String paymentId,
		String method,
		BigDecimal amount,
		PaymentStatus status,
		LocalDateTime paidAt,
		@Schema(nullable = true) BigDecimal cashGiven,
		@Schema(nullable = true) BigDecimal change,
		@Schema(nullable = true) String provider,
		@Schema(nullable = true) String accountNumber) {

	public static PaymentResponse from(Payment payment) {
		BigDecimal cashGiven = null;
		BigDecimal change = null;
		String provider = null;
		String accountNumber = null;
		if (payment instanceof CashPayment cash) {
			cashGiven = cash.getCashGiven();
			change = cash.getChange();
		} else if (payment instanceof EWalletPayment ewallet) {
			provider = ewallet.getProvider();
			accountNumber = maskAccount(ewallet.getAccountNumber());
		}
		return new PaymentResponse(payment.getPaymentId(), payment.getMethodName(),
				payment.getAmount(), payment.getStatus(), payment.getPaidAt(),
				cashGiven, change, provider, accountNumber);
	}

	private static String maskAccount(String accountNumber) {
		if (accountNumber == null || accountNumber.length() <= 4) {
			return "****";
		}
		return "****" + accountNumber.substring(accountNumber.length() - 4);
	}
}
