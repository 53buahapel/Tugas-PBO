package com.tugaspbo.backend.dto.request;

import java.math.BigDecimal;

import com.tugaspbo.backend.enums.MenuCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MenuItemRequest(
		@NotBlank(message = "Name is required") String name,
		@NotNull(message = "Price is required") @Positive(message = "Price must be greater than zero") BigDecimal price,
		@NotNull(message = "Category is required") MenuCategory category,
		Boolean available) {
}
