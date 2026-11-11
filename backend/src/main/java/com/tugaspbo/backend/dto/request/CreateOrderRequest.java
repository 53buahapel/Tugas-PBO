package com.tugaspbo.backend.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
		@NotEmpty(message = "Order must contain at least one item") List<@Valid OrderItemRequest> items) {
}
