package com.tugaspbo.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugaspbo.backend.dto.request.MenuItemRequest;
import com.tugaspbo.backend.dto.request.UpdateAvailabilityRequest;
import com.tugaspbo.backend.dto.request.UpdatePriceRequest;
import com.tugaspbo.backend.dto.response.ApiResponse;
import com.tugaspbo.backend.dto.response.MenuItemResponse;
import com.tugaspbo.backend.enums.MenuCategory;
import com.tugaspbo.backend.service.MenuService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Menu")
public class MenuController {

	private final MenuService menuService;

	public MenuController(MenuService menuService) {
		this.menuService = menuService;
	}

	@GetMapping
	@Operation(summary = "Get all menu items")
	public ApiResponse<List<MenuItemResponse>> getAll() {
		return ApiResponse.ok("Menu items retrieved", menuService.getAllItems());
	}

	@GetMapping("/available")
	@Operation(summary = "Get only available menu items")
	public ApiResponse<List<MenuItemResponse>> getAvailable() {
		return ApiResponse.ok("Available menu items retrieved", menuService.getAvailableItems());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get menu item details")
	public ApiResponse<MenuItemResponse> getById(@PathVariable String id) {
		return ApiResponse.ok("Menu item retrieved", menuService.getById(id));
	}

	@GetMapping("/category/{category}")
	@Operation(summary = "Filter menu items by category")
	public ApiResponse<List<MenuItemResponse>> getByCategory(@PathVariable MenuCategory category) {
		return ApiResponse.ok("Menu items retrieved", menuService.getByCategory(category));
	}

	@PostMapping
	@Operation(summary = "Add a menu item (admin only)")
	public ResponseEntity<ApiResponse<MenuItemResponse>> create(
			@Valid @RequestBody MenuItemRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Menu item created", menuService.createItem(request)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a menu item (admin only)")
	public ApiResponse<MenuItemResponse> update(@PathVariable String id,
			@Valid @RequestBody MenuItemRequest request) {
		return ApiResponse.ok("Menu item updated", menuService.updateItem(id, request));
	}

	@PatchMapping("/{id}/price")
	@Operation(summary = "Update menu price (admin only)")
	public ApiResponse<MenuItemResponse> updatePrice(@PathVariable String id,
			@Valid @RequestBody UpdatePriceRequest request) {
		return ApiResponse.ok("Menu price updated", menuService.updatePrice(id, request.price()));
	}

	@PatchMapping("/{id}/availability")
	@Operation(summary = "Update menu availability (admin only)")
	public ApiResponse<MenuItemResponse> setAvailability(@PathVariable String id,
			@Valid @RequestBody UpdateAvailabilityRequest request) {
		return ApiResponse.ok("Menu availability updated",
				menuService.setAvailability(id, request.available()));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a menu item (admin only)")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
		menuService.deleteItem(id);
		return ResponseEntity.noContent().build();
	}
}
