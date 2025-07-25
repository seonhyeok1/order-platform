package app.domain.menu.model.dto.request;

import java.util.UUID;

public record MenuListRequest(
	UUID storeId
) {
}
