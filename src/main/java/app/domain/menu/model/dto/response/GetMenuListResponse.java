package app.domain.menu.model.dto.response;

import java.util.UUID;

import app.domain.menu.model.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMenuListResponse {
	private UUID menuId;
	private String name;
	private String description;
	private Long price;

	public static GetMenuListResponse from(Menu menu) {
		return new GetMenuListResponse(
			menu.getMenuId(),
			menu.getName(),
			menu.getDescription(),
			menu.getPrice()
		);
	}
}