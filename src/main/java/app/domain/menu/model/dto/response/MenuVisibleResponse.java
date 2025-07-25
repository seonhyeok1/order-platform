package app.domain.menu.model.dto.response;

import java.util.UUID;

public record MenuVisibleResponse(
	UUID menuId,
	Boolean visible
) {
}
