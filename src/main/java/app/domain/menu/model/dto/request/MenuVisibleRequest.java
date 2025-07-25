package app.domain.menu.model.dto.request;

import java.util.UUID;

public record MenuVisibleRequest(
	UUID menuId,
	Boolean visible
) {
}
