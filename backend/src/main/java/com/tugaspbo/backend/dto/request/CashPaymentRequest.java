package com.tugaspbo.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CashPaymentRequest(
		@NotNull(message = "Cash given is required") @Positive(message = "Cash given must be greater than zero") BigDecimal cashGiven) {
}
