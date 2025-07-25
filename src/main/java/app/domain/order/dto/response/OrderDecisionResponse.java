package app.domain.order.dto.response;

import java.util.UUID;

public record OrderDecisionResponse(
	UUID orderId,
	String status
) {
}
