package app.domain.order.model.dto.response;

import java.util.UUID;

public record OrderDecisionResponse(
	UUID orderId,
	String status
) {
}
