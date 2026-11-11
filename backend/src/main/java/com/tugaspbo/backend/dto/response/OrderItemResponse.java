package com.tugaspbo.backend.dto.response;

import java.math.BigDecimal;

import com.tugaspbo.backend.entity.OrderItem;

public record OrderItemResponse(
		String menuItemId,
		String itemName,
		BigDecimal unitPrice,
		int quantity,
		BigDecimal subtotal) {

	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(item.getMenuItem().getId(), item.getItemName(),
				item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
	}
}
