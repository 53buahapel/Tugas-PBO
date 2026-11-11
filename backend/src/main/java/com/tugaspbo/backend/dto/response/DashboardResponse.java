package com.tugaspbo.backend.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
		long totalOrders,
		long pendingOrders,
		long paidOrders,
		long processingOrders,
		long completedOrders,
		BigDecimal totalRevenue) {
}
