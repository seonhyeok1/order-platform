package app.domain.cart.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.cart.model.dto.request.AddCartItemRequest;
import app.domain.cart.model.dto.response.RedisCartItem;
import app.domain.cart.service.CartService;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "cart", description = "장바구니 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	@Operation(summary = "장바구니 아이템 추가 API", description = "장바구니에 메뉴 아이템을 추가합니다. 다른 매장의 메뉴 추가 시 기존 장바구니는 초기화됩니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 장바구니 Redis 저장 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART001",
					    "message": "장바구니 Redis 저장에 실패했습니다."
					}
					""")))
	})
	@PostMapping("/item")
	public ApiResponse<Void> addItemToCart(@RequestParam Long userId, @RequestBody AddCartItemRequest request) {
		if (request.quantity() <= 0) {
			throw new GeneralException(ErrorStatus.INVALID_QUANTITY);
		}
		cartService.addCartItem(userId, request.menuId(), request.storeId(), request.quantity());
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "장바구니 아이템 수량 수정 API", description = "장바구니에 있는 특정 메뉴의 수량을 수정합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 장바구니 Redis 저장 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART001",
					    "message": "장바구니 Redis 저장에 실패했습니다."
					}
					""")))
	})
	@PatchMapping("/item/{menuId}/{quantity}")
	public ApiResponse<Void> updateItemInCart(@RequestParam Long userId, @PathVariable UUID menuId,
		@PathVariable int quantity) {
		cartService.updateCartItem(userId, menuId, quantity);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "장바구니 아이템 삭제 API", description = "장바구니에서 특정 메뉴를 삭제합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 장바구니 Redis 저장 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART001",
					    "message": "장바구니 Redis 저장에 실패했습니다."
					}
					""")))
	})
	@DeleteMapping("/item/{menuId}")
	public ApiResponse<Void> removeItemFromCart(@RequestParam Long userId, @PathVariable UUID menuId) {
		cartService.removeCartItem(userId, menuId);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "장바구니 조회 API", description = "사용자의 장바구니 내용을 조회합니다. Redis에 없으면 DB에서 로드합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공. 장바구니 아이템 목록을 반환합니다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON200",
					    "message": "success",
					    "result": [
					        {
					            "menuId": "550e8400-e29b-41d4-a716-446655440000",
					            "storeId": "550e8400-e29b-41d4-a716-446655440001",
					            "quantity": 2
					        },
					        {
					            "menuId": "550e8400-e29b-41d4-a716-446655440002",
					            "storeId": "550e8400-e29b-41d4-a716-446655440001",
					            "quantity": 1
					        }
					    ]
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 장바구니 Redis 조회 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART002",
					    "message": "장바구니 Redis 조회에 실패했습니다."
					}
					""")))
	})
	@GetMapping()
	public ApiResponse<List<RedisCartItem>> getCart(@RequestParam Long userId) {
		List<RedisCartItem> cartItems = cartService.getCartFromCache(userId);
		return ApiResponse.onSuccess(cartItems);
	}

	@Operation(summary = "장바구니 전체 삭제 API", description = "사용자의 장바구니를 전체 삭제합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 장바구니 Redis 저장 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART001",
					    "message": "장바구니 Redis 저장에 실패했습니다."
					}
					""")))
	})
	@DeleteMapping("/item")
	public ApiResponse<Void> clearCart(@RequestParam Long userId) {
		cartService.clearCartItems(userId);
		return ApiResponse.onSuccess(null);
	}

}
