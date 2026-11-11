package com.tugaspbo.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.tugaspbo.backend.enums.PaymentStatus;

class PaymentEntityTest {

	@Test
	void cashPaymentSucceedsAndCalculatesChange() {
		CashPayment payment = new CashPayment(BigDecimal.valueOf(61000), BigDecimal.valueOf(70000));

		assertTrue(payment.pay());
		assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
		assertEquals(0, payment.getChange().compareTo(BigDecimal.valueOf(9000)));
		assertNotNull(payment.getPaidAt());
		assertEquals("CASH", payment.getMethodName());
	}

	@Test
	void cashPaymentRejectsInsufficientCash() {
		CashPayment payment = new CashPayment(BigDecimal.valueOf(61000), BigDecimal.valueOf(50000));

		assertFalse(payment.pay());
		assertEquals(PaymentStatus.FAILED, payment.getStatus());
	}

	@Test
	void paymentCanOnlyBeProcessedOnce() {
		CashPayment payment = new CashPayment(BigDecimal.valueOf(10000), BigDecimal.valueOf(10000));
		assertTrue(payment.pay());

		assertThrows(IllegalStateException.class, payment::pay);
	}

	@Test
	void eWalletPaymentSucceedsForSupportedProvider() {
		EWalletPayment payment = new EWalletPayment(BigDecimal.valueOf(50000), "DANA",
				"081234567890");

		assertTrue(payment.pay());
		assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
		assertEquals("E-WALLET", payment.getMethodName());
	}

	@Test
	void eWalletPaymentRejectsUnsupportedProvider() {
		EWalletPayment payment = new EWalletPayment(BigDecimal.valueOf(50000), "PAYPAL",
				"081234567890");

		assertFalse(payment.pay());
		assertEquals(PaymentStatus.FAILED, payment.getStatus());
	}

	@Test
	void eWalletPaymentRejectsInvalidAccountNumber() {
		EWalletPayment payment = new EWalletPayment(BigDecimal.valueOf(50000), "OVO", "not-a-number");

		assertFalse(payment.pay());
		assertEquals(PaymentStatus.FAILED, payment.getStatus());
	}
}
