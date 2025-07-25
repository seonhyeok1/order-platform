package app.domain.menu.dto.request;

import java.util.UUID;

public record MenuVisibleRequest(
	UUID menuId,
	Boolean visible
) {
}
