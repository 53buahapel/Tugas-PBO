package com.tugaspbo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EWalletPaymentRequest(
		@NotBlank(message = "Provider is required") String provider,
		@NotBlank(message = "Account number is required") String accountNumber) {
}
