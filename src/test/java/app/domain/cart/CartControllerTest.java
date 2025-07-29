package app.domain.cart;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.cart.model.dto.AddCartItemRequest;
import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartService;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private CartService cartService;

	@BeforeEach
	void setUp() {
		CartController cartController = new CartController(cartService);
		mockMvc = MockMvcBuilders.standaloneSetup(cartController)
			.setControllerAdvice(new TestExceptionHandler())
			.build();
		objectMapper = new ObjectMapper();
	}

	@RestControllerAdvice
	static class TestExceptionHandler {
		@ExceptionHandler(GeneralException.class)
		public ResponseEntity<ApiResponse<Object>> handleGeneralException(GeneralException e) {
			return ResponseEntity.status(e.getErrorReasonHttpStatus().getHttpStatus())
				.body(ApiResponse.onFailure(e.getErrorReasonHttpStatus().getCode(), e.getMessage(), null));
		}
	}

	@Test
	@DisplayName("장바구니 아이템 추가 - 성공")
	void addItemToCart_Success() throws Exception {
		Long userId = 1L;
		UUID menuId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		AddCartItemRequest request = new AddCartItemRequest(menuId, storeId, 2);

		when(cartService.addCartItem(userId, menuId, storeId, 2))
			.thenReturn("사용자 1의 장바구니가 성공적으로 저장되었습니다.");

		mockMvc.perform(post("/cart/item")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value("사용자 1의 장바구니가 성공적으로 저장되었습니다."));

		verify(cartService).addCartItem(userId, menuId, storeId, 2);
	}

	@Test
	@DisplayName("장바구니 아이템 추가 - 수량 0 이하 실패")
	void addItemToCart_InvalidQuantity() throws Exception {
		Long userId = 1L;
		UUID menuId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		AddCartItemRequest request = new AddCartItemRequest(menuId, storeId, 0);

		mockMvc.perform(post("/cart/item")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.resultCode").value("CART006"));
		// .andExpect(jsonPath("$.message").value("수량은 1 이상이어야 합니다."));

		verify(cartService, never()).addCartItem(any(), any(), any(), anyInt());
	}

	@Test
	@DisplayName("장바구니 아이템 추가 - 서비스 에러")
	void addItemToCart_ServiceError() throws Exception {
		Long userId = 1L;
		UUID menuId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		AddCartItemRequest request = new AddCartItemRequest(menuId, storeId, 2);

		when(cartService.addCartItem(userId, menuId, storeId, 2))
			.thenThrow(new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED));

		mockMvc.perform(post("/cart/item")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.resultCode").value("CART001"));
		// .andExpect(jsonPath("$.message").value("장바구니 Redis 저장에 실패했습니다."));
	}

	@Test
	@DisplayName("장바구니 아이템 수량 수정 - 성공")
	void updateItemInCart_Success() throws Exception {
		Long userId = 1L;
		UUID menuId = UUID.randomUUID();
		int quantity = 5;

		when(cartService.updateCartItem(userId, menuId, quantity))
			.thenReturn("사용자 1의 장바구니가 성공적으로 저장되었습니다.");

		mockMvc.perform(patch("/cart/item/{menuId}/{quantity}", menuId, quantity)
				.param("userId", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value("사용자 1의 장바구니가 성공적으로 저장되었습니다."));

		verify(cartService).updateCartItem(userId, menuId, quantity);
	}

	@Test
	@DisplayName("장바구니 아이템 삭제 - 성공")
	void removeItemFromCart_Success() throws Exception {
		Long userId = 1L;
		UUID menuId = UUID.randomUUID();

		when(cartService.removeCartItem(userId, menuId))
			.thenReturn("사용자 1의 장바구니에서 메뉴가 성공적으로 삭제되었습니다.");

		mockMvc.perform(delete("/cart/item/{menuId}", menuId)
				.param("userId", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value("사용자 1의 장바구니에서 메뉴가 성공적으로 삭제되었습니다."));

		verify(cartService).removeCartItem(userId, menuId);
	}

	@Test
	@DisplayName("장바구니 조회 - 성공")
	void getCart_Success() throws Exception {
		Long userId = 1L;
		UUID menuId1 = UUID.randomUUID();
		UUID menuId2 = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();

		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder().menuId(menuId1).storeId(storeId).quantity(2).build(),
			RedisCartItem.builder().menuId(menuId2).storeId(storeId).quantity(1).build()
		);

		when(cartService.getCartFromCache(userId)).thenReturn(cartItems);

		mockMvc.perform(get("/cart")
				.param("userId", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").isArray())
			.andExpect(jsonPath("$.result.length()").value(2))
			.andExpect(jsonPath("$.result[0].menuId").value(menuId1.toString()))
			.andExpect(jsonPath("$.result[0].quantity").value(2))
			.andExpect(jsonPath("$.result[1].menuId").value(menuId2.toString()))
			.andExpect(jsonPath("$.result[1].quantity").value(1));

		verify(cartService).getCartFromCache(userId);
	}

	@Test
	@DisplayName("장바구니 조회 - 빈 장바구니")
	void getCart_Empty() throws Exception {
		Long userId = 1L;

		when(cartService.getCartFromCache(userId)).thenReturn(List.of());

		mockMvc.perform(get("/cart")
				.param("userId", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").isArray())
			.andExpect(jsonPath("$.result.length()").value(0));

		verify(cartService).getCartFromCache(userId);
	}

	@Test
	@DisplayName("장바구니 조회 - 서비스 에러")
	void getCart_ServiceError() throws Exception {
		Long userId = 1L;

		when(cartService.getCartFromCache(userId))
			.thenThrow(new GeneralException(ErrorStatus.CART_REDIS_LOAD_FAILED));

		mockMvc.perform(get("/cart")
				.param("userId", userId.toString()))
			.andDo(print())
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.resultCode").value("CART002"));
		// .andExpect(jsonPath("$.message").value("장바구니 Redis 조회에 실패했습니다."));
	}

	@Test
	@DisplayName("장바구니 전체 삭제 - 성공")
	void clearCart_Success() throws Exception {
		Long userId = 1L;

		when(cartService.clearCartItems(userId))
			.thenReturn("사용자 1의 장바구니가 성공적으로 비워졌습니다.");

		mockMvc.perform(delete("/cart/item")
				.param("userId", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value("사용자 1의 장바구니가 성공적으로 비워졌습니다."));

		verify(cartService).clearCartItems(userId);
	}

	@Test
	@DisplayName("장바구니 전체 삭제 - 서비스 에러")
	void clearCart_ServiceError() throws Exception {
		Long userId = 1L;

		when(cartService.clearCartItems(userId))
			.thenThrow(new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED));

		mockMvc.perform(delete("/cart/item")
				.param("userId", userId.toString()))
			.andDo(print())
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.resultCode").value("CART001"));
		// .andExpect(jsonPath("$.message").value("장바구니 Redis 저장에 실패했습니다."));
	}

	@Test
	@DisplayName("잘못된 JSON 형식 - 요청 바디 매핑 실패")
	void addItemToCart_InvalidJson() throws Exception {
		Long userId = 1L;
		String invalidJson = "{\"menuId\": \"invalid-uuid\", \"storeId\": \"valid-uuid\", \"quantity\": 2}";

		mockMvc.perform(post("/cart/item")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andExpect(status().isBadRequest());

		verify(cartService, never()).addCartItem(any(), any(), any(), anyInt());
	}

	@Test
	@DisplayName("필수 파라미터 누락 - userId 없음")
	void addItemToCart_MissingUserId() throws Exception {
		UUID menuId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		AddCartItemRequest request = new AddCartItemRequest(menuId, storeId, 2);

		mockMvc.perform(post("/cart/item")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(cartService, never()).addCartItem(any(), any(), any(), anyInt());
	}
}