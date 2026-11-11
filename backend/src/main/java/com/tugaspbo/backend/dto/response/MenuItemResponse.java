package com.tugaspbo.backend.dto.response;

import java.math.BigDecimal;

import com.tugaspbo.backend.entity.MenuItem;
import com.tugaspbo.backend.enums.MenuCategory;

public record MenuItemResponse(
		String id,
		String name,
		BigDecimal price,
		MenuCategory category,
		boolean available) {

	public static MenuItemResponse from(MenuItem item) {
		return new MenuItemResponse(item.getId(), item.getName(), item.getPrice(),
				item.getCategory(), item.isAvailable());
	}
}
