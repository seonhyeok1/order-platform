package app.domain.menu.model.dto.response;

import java.sql.Array;
import java.util.UUID;

public record MenuListResponse(
	UUID menuId,
	String name,
	Long price,
	Array menuList
) {
}
