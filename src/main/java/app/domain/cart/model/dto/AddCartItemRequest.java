package app.domain.cart.model.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(

	@NotNull
	@Schema(description = "메뉴ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	UUID menuId,

	@NotNull
	@Schema(description = "매장ID", example = "3fa85f64-5727-4562-b3gc-2c963f66afa6")
	UUID storeId,

	@NotNull
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	@Schema(description = "메뉴 개수", example = "1")
	Integer quantity
) {
}