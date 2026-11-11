package com.tugaspbo.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdatePriceRequest(
		@NotNull(message = "Price is required") @Positive(message = "Price must be greater than zero") BigDecimal price) {
}
