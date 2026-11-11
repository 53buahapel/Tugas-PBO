package com.tugaspbo.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.tugaspbo.backend.enums.MenuCategory;
import com.tugaspbo.backend.enums.UserRole;

class MenuDomainTest {

	@Test
	void menuManagesItemCollection() {
		Menu menu = new Menu("Cafe Menu");
		MenuItem item = new MenuItem("MENU-1", "Iced Coffee", BigDecimal.valueOf(18000),
				MenuCategory.DRINK);
		menu.addItem(item);

		assertTrue(menu.findById("MENU-1").isPresent());
		assertEquals(1, menu.getAvailableItems().size());
		assertFalse(menu.findById("MENU-999").isPresent());
		assertTrue(menu.removeItem("MENU-1"));
		assertTrue(menu.findById("MENU-1").isEmpty());
	}

	@Test
	void getAvailableItemsExcludesUnavailableAndDeleted() {
		Menu menu = new Menu("Cafe Menu");
		MenuItem available = new MenuItem("MENU-1", "Coffee", BigDecimal.valueOf(18000),
				MenuCategory.DRINK);
		MenuItem unavailable = new MenuItem("MENU-2", "Cake", BigDecimal.valueOf(28000),
				MenuCategory.DESSERT);
		unavailable.setAvailable(false);
		menu.addItem(available);
		menu.addItem(unavailable);

		assertEquals(1, menu.getAvailableItems().size());
		assertEquals("MENU-1", menu.getAvailableItems().get(0).getId());
	}

	@Test
	void priceMustBePositive() {
		MenuItem item = new MenuItem("MENU-1", "Coffee", BigDecimal.valueOf(18000),
				MenuCategory.DRINK);

		assertThrows(IllegalArgumentException.class, () -> item.updatePrice(0));
		assertThrows(IllegalArgumentException.class, () -> item.updatePrice(-5000));
		assertThrows(IllegalArgumentException.class,
				() -> new MenuItem("MENU-2", "Tea", BigDecimal.ZERO, MenuCategory.DRINK));
	}

	@Test
	void nameMustNotBeEmpty() {
		assertThrows(IllegalArgumentException.class,
				() -> new MenuItem("MENU-1", "  ", BigDecimal.valueOf(1000), MenuCategory.FOOD));
	}

	@Test
	void orderItemSubtotalAndQuantityRules() {
		Customer customer = new Customer("c1", "Budi", "b@x.com", "pass123", "0812");
		Order order = customer.createOrder();
		MenuItem item = new MenuItem("MENU-1", "Coffee", BigDecimal.valueOf(18000),
				MenuCategory.DRINK);
		OrderItem orderItem = new OrderItem(order, item, 3);

		assertEquals(0, orderItem.getSubtotal().compareTo(BigDecimal.valueOf(54000)));
		orderItem.increaseQuantity(2);
		assertEquals(5, orderItem.getQuantity());
		assertThrows(IllegalArgumentException.class, () -> orderItem.increaseQuantity(0));
		assertThrows(IllegalArgumentException.class, () -> new OrderItem(order, item, 0));
	}

	@Test
	void userLoginVerifiesBcryptPassword() {
		Customer customer = new Customer("c1", "Budi", "b@x.com", "pass123", "0812");

		assertTrue(customer.login("b@x.com", "pass123"));
		assertFalse(customer.login("b@x.com", "wrong"));
		assertFalse(customer.login("other@x.com", "pass123"));
		assertTrue(customer.getPassword().startsWith("$2"));
		assertEquals(UserRole.CUSTOMER.name(), customer.getRole());
	}

	@Test
	void adminManagesItsMenu() {
		Menu menu = new Menu("Cafe Menu");
		Admin admin = new Admin("a1", "Admin", "a@x.com", "pass123", menu);
		MenuItem item = new MenuItem("MENU-1", "Coffee", BigDecimal.valueOf(18000),
				MenuCategory.DRINK);

		admin.addMenuItem(item);
		admin.updateMenuPrice("MENU-1", 20000);
		admin.setMenuAvailability("MENU-1", false);

		assertEquals(0, item.getPrice().compareTo(BigDecimal.valueOf(20000)));
		assertFalse(item.isAvailable());
		assertEquals(UserRole.ADMIN.name(), admin.getRole());

		admin.removeMenuItem("MENU-1");
		assertTrue(menu.findById("MENU-1").isEmpty());
		assertThrows(IllegalArgumentException.class, () -> admin.removeMenuItem("MENU-999"));
	}
}
