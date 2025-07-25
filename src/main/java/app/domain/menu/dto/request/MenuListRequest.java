package app.domain.menu.dto.request;

import java.util.UUID;

public record MenuListRequest(
	UUID storeId
) {
}
