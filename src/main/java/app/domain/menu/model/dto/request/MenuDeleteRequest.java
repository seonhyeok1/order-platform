package app.domain.menu.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class MenuDeleteRequest {
	@NotNull
	private UUID menuId;

	public MenuDeleteRequest() {
	}

	public MenuDeleteRequest(UUID menuId) {
		this.menuId = menuId;
	}

	public UUID getMenuId() {
		return menuId;
	}

	public void setMenuId(UUID menuId) {
		this.menuId = menuId;
	}
}
