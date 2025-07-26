package app.domain.cart.model.dto.request;

import java.util.UUID;

import lombok.Data;

@Data
public class AddCartItemRequest {
	private UUID menuId;
	private int quantity;
}
