package com.tugaspbo.backend.config;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.entity.Admin;
import com.tugaspbo.backend.entity.Menu;
import com.tugaspbo.backend.entity.MenuItem;
import com.tugaspbo.backend.enums.MenuCategory;
import com.tugaspbo.backend.repository.AdminRepository;
import com.tugaspbo.backend.repository.MenuRepository;
import com.tugaspbo.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

	private final MenuRepository menuRepository;
	private final AdminRepository adminRepository;
	private final UserRepository userRepository;
	private final String adminEmail;
	private final String adminPassword;

	public DataInitializer(MenuRepository menuRepository, AdminRepository adminRepository,
			UserRepository userRepository,
			@Value("${app.seed.admin-email}") String adminEmail,
			@Value("${app.seed.admin-password}") String adminPassword) {
		this.menuRepository = menuRepository;
		this.adminRepository = adminRepository;
		this.userRepository = userRepository;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Override
	@Transactional
	public void run(String... args) {
		Menu menu = menuRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
			Menu m = new Menu("Cafe Menu");
			m.addItem(item("MENU-001", "Nasi Goreng", 25000, MenuCategory.FOOD));
			m.addItem(item("MENU-002", "Ayam Geprek", 20000, MenuCategory.FOOD));
			m.addItem(item("MENU-003", "Iced Coffee", 18000, MenuCategory.DRINK));
			m.addItem(item("MENU-004", "Matcha Latte", 22000, MenuCategory.DRINK));
			m.addItem(item("MENU-005", "Chocolate Cake", 28000, MenuCategory.DESSERT));
			return menuRepository.save(m);
		});

		if (!userRepository.existsByEmail(adminEmail)) {
			adminRepository.save(
					new Admin(UUID.randomUUID().toString(), "Admin", adminEmail, adminPassword, menu));
		}
	}

	private MenuItem item(String id, String name, double price, MenuCategory category) {
		return new MenuItem(id, name, BigDecimal.valueOf(price), category);
	}
}
