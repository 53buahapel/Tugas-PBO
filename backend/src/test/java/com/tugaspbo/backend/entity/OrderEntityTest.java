package com.tugaspbo.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tugaspbo.backend.enums.MenuCategory;
import com.tugaspbo.backend.enums.OrderStatus;
import com.tugaspbo.backend.enums.PaymentStatus;

class OrderEntityTest {

	private Customer customer;
	private MenuItem coffee;
	private MenuItem rice;
	private Order order;

	@BeforeEach
	void setUp() {
		customer = new Customer("c1", "Budi", "budi@example.com", "secret123", "08123456789");
		coffee = new MenuItem("MENU-003", "Iced Coffee", BigDecimal.valueOf(18000), MenuCategory.DRINK);
		rice = new MenuItem("MENU-001", "Nasi Goreng", BigDecimal.valueOf(25000), MenuCategory.FOOD);
		order = customer.createOrder();
	}

	@Test
	void addItemCalculatesTotalForMultipleItems() {
		order.addItem(coffee, 2);
		order.addItem(rice, 1);

		assertEquals(0, order.calculateTotal().compareTo(BigDecimal.valueOf(61000)));
		assertEquals(2, order.getItems().size());
		assertEquals(OrderStatus.PENDING, order.getStatus());
		assertNotNull(order.getCreatedAt());
	}

	@Test
	void addingSameItemTwiceMergesQuantity() {
		order.addItem(coffee, 1);
		order.addItem(coffee, 2);

		assertEquals(1, order.getItems().size());
		assertEquals(3, order.getItems().get(0).getQuantity());
	}

	@Test
	void rejectsUnavailableMenuItem() {
		coffee.setAvailable(false);

		assertThrows(IllegalArgumentException.class, () -> order.addItem(coffee, 1));
	}

	@Test
	void rejectsZeroOrNegativeQuantity() {
		assertThrows(IllegalArgumentException.class, () -> order.addItem(coffee, 0));
		assertThrows(IllegalArgumentException.class, () -> order.addItem(coffee, -1));
	}

	@Test
	void orderItemPreservesHistoricalPriceAfterMenuPriceUpdate() {
		order.addItem(coffee, 1);
		coffee.updatePrice(30000);

		OrderItem item = order.getItems().get(0);
		assertEquals(0, item.getUnitPrice().compareTo(BigDecimal.valueOf(18000)));
		assertEquals(0, order.calculateTotal().compareTo(BigDecimal.valueOf(18000)));
	}

	@Test
	void removeItemUpdatesTotal() {
		order.addItem(coffee, 2);
		order.addItem(rice, 1);

		assertTrue(order.removeItem("MENU-003"));
		assertEquals(0, order.calculateTotal().compareTo(BigDecimal.valueOf(25000)));
		assertFalse(order.removeItem("MENU-999"));
	}

	@Test
	void checkoutRejectsEmptyOrder() {
		assertThrows(IllegalStateException.class,
				() -> order.checkout(new CashPayment(BigDecimal.ZERO, BigDecimal.TEN)));
	}

	@Test
	void checkoutRejectsMismatchedPaymentAmount() {
		order.addItem(coffee, 1);

		assertThrows(IllegalArgumentException.class,
				() -> order.checkout(new CashPayment(BigDecimal.valueOf(99999),
						BigDecimal.valueOf(99999))));
	}

	@Test
	void checkoutMarksOrderPaidOnSuccessfulPayment() {
		order.addItem(coffee, 2);
		Payment payment = new CashPayment(order.calculateTotal(), BigDecimal.valueOf(50000));

		assertTrue(order.checkout(payment));
		assertEquals(OrderStatus.PAID, order.getStatus());
		assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
		assertEquals(payment, order.getPayment());
	}

	@Test
	void failedPaymentKeepsOrderPending() {
		order.addItem(coffee, 1);
		Payment payment = new CashPayment(order.calculateTotal(), BigDecimal.valueOf(1000));

		assertFalse(order.checkout(payment));
		assertEquals(OrderStatus.PENDING, order.getStatus());
		assertEquals(PaymentStatus.FAILED, payment.getStatus());
	}

	@Test
	void enforcesAllowedStatusTransitions() {
		assertThrows(IllegalStateException.class, () -> order.markProcessing());
		assertThrows(IllegalStateException.class, () -> order.markCompleted());

		order.addItem(coffee, 1);
		order.checkout(new CashPayment(order.calculateTotal(), BigDecimal.valueOf(50000)));

		order.markProcessing();
		assertEquals(OrderStatus.PROCESSING, order.getStatus());
		order.markCompleted();
		assertEquals(OrderStatus.COMPLETED, order.getStatus());
		assertThrows(IllegalStateException.class, () -> order.cancel());
		assertThrows(IllegalStateException.class, () -> order.markProcessing());
	}

	@Test
	void paidOrderCannotBeCancelledWithoutRefund() {
		order.addItem(coffee, 1);
		order.checkout(new CashPayment(order.calculateTotal(), BigDecimal.valueOf(50000)));

		assertThrows(IllegalStateException.class, () -> order.cancel());
	}

	@Test
	void pendingOrderCanBeCancelled() {
		order.cancel();
		assertEquals(OrderStatus.CANCELLED, order.getStatus());
		assertThrows(IllegalStateException.class, () -> order.addItem(coffee, 1));
	}
}
