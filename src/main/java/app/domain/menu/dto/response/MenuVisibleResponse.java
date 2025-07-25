package app.domain.menu.dto.response;

import java.util.UUID;

public record MenuVisibleResponse(
	UUID menuId,
	Boolean visible
) {
}
