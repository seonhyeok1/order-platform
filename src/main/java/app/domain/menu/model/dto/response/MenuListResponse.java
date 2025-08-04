package app.domain.menu.model.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MenuListResponse {
	private UUID storeId;
	private List<MenuDetail> menus;

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	public static class MenuDetail {
		private UUID menuId;
		private String name;
		private Long price;
		private String description;
		private boolean isHidden;
	}
}