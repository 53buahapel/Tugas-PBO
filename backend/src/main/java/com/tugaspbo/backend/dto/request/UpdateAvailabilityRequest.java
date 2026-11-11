package com.tugaspbo.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityRequest(
		@NotNull(message = "Availability is required") Boolean available) {
}
