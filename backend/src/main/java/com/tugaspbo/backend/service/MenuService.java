package com.tugaspbo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tugaspbo.backend.dto.request.MenuItemRequest;
import com.tugaspbo.backend.dto.response.MenuItemResponse;
import com.tugaspbo.backend.entity.Menu;
import com.tugaspbo.backend.entity.MenuItem;
import com.tugaspbo.backend.enums.MenuCategory;
import com.tugaspbo.backend.exception.ResourceNotFoundException;
import com.tugaspbo.backend.repository.MenuItemRepository;
import com.tugaspbo.backend.repository.MenuRepository;
import com.tugaspbo.backend.repository.OrderItemRepository;

@Service
public class MenuService {

	private final MenuRepository menuRepository;
	private final MenuItemRepository menuItemRepository;
	private final OrderItemRepository orderItemRepository;

	public MenuService(MenuRepository menuRepository, MenuItemRepository menuItemRepository,
			OrderItemRepository orderItemRepository) {
		this.menuRepository = menuRepository;
		this.menuItemRepository = menuItemRepository;
		this.orderItemRepository = orderItemRepository;
	}

	@Transactional(readOnly = true)
	public List<MenuItemResponse> getAllItems() {
		return menuItemRepository.findByDeletedFalse().stream().map(MenuItemResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<MenuItemResponse> getAvailableItems() {
		return menuItemRepository.findByAvailableTrueAndDeletedFalse().stream()
				.map(MenuItemResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public MenuItemResponse getById(String id) {
		return MenuItemResponse.from(findItem(id));
	}

	@Transactional(readOnly = true)
	public List<MenuItemResponse> getByCategory(MenuCategory category) {
		return menuItemRepository.findByCategoryAndDeletedFalse(category).stream()
				.map(MenuItemResponse::from).toList();
	}

	@Transactional
	public MenuItemResponse createItem(MenuItemRequest request) {
		MenuItem item = new MenuItem(request.name(), request.price(), request.category());
		if (request.available() != null) {
			item.setAvailable(request.available());
		}
		getMenu().addItem(item);
		menuItemRepository.save(item);
		return MenuItemResponse.from(item);
	}

	@Transactional
	public MenuItemResponse updateItem(String id, MenuItemRequest request) {
		MenuItem item = findItem(id);
		item.setName(request.name());
		item.updatePrice(request.price());
		item.setCategory(request.category());
		if (request.available() != null) {
			item.setAvailable(request.available());
		}
		return MenuItemResponse.from(menuItemRepository.save(item));
	}

	@Transactional
	public MenuItemResponse updatePrice(String id, java.math.BigDecimal price) {
		MenuItem item = findItem(id);
		item.updatePrice(price);
		return MenuItemResponse.from(menuItemRepository.save(item));
	}

	@Transactional
	public MenuItemResponse setAvailability(String id, boolean available) {
		MenuItem item = findItem(id);
		item.setAvailable(available);
		return MenuItemResponse.from(menuItemRepository.save(item));
	}

	@Transactional
	public void deleteItem(String id) {
		MenuItem item = findItem(id);
		if (orderItemRepository.existsByMenuItemId(id)) {
			item.setDeleted(true);
			item.setAvailable(false);
			menuItemRepository.save(item);
		} else {
			getMenu().removeItem(id);
			menuItemRepository.delete(item);
		}
	}

	private MenuItem findItem(String id) {
		return menuItemRepository.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
	}

	private Menu getMenu() {
		return menuRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new ResourceNotFoundException("Menu is not initialized"));
	}
}
