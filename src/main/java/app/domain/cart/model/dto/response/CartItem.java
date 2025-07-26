package app.domain.cart.model.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CartItem {

	private List<CartItemResponse> items;

	@Data
	@AllArgsConstructor
	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
	public static class CartItemResponse {
		private UUID menuId;
		private int quantity;
	}
}
