package app.domain.cart.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.cart.model.CartItemRepository;
import app.domain.cart.model.CartRepository;
import app.domain.cart.model.dto.response.RedisCartItem;
import app.domain.cart.model.entity.Cart;
import app.domain.cart.model.entity.CartItem;
import app.domain.cart.service.impl.CartServiceImpl;
import app.domain.menu.model.entity.Menu;
import app.domain.store.model.entity.Store;
import app.domain.user.model.entity.User;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartRedisService cartRedisService;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@InjectMocks
	private CartServiceImpl cartService;

	private Long userId;
	private UUID menuId;
	private UUID storeId;
	private List<RedisCartItem> cartItems;

	@BeforeEach
	void setUp() {
		userId = 1L;
		menuId = UUID.randomUUID();
		storeId = UUID.randomUUID();
		cartItems = new ArrayList<>();
	}

	@Test
	@DisplayName("장바구니에 새로운 아이템을 추가할 수 있다")
	void addItem() {
		// given - 빈 장바구니 상태를 모킹
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when - 새로운 메뉴 아이템을 수량 2개로 추가
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then - Redis에 올바른 아이템이 저장되는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 && // 1개의 아이템이 저장되어야 함
				items.get(0).getMenuId().equals(menuId) && // 메뉴 ID가 일치해야 함
				items.get(0).getQuantity() == 2 // 수량이 2개여야 함
		));
	}

	@Test
	@DisplayName("이미 존재하는 아이템을 추가하면 수량이 누적된다")
	void addExistingItem() {
		// given - 이미 수량 1개인 아이템이 장바구니에 있는 상태
		cartItems.add(RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when - 동일한 메뉴를 수량 2개로 다시 추가
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then - 기존 수량(1) + 새 수량(2) = 3개로 업데이트되는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.get(0).getQuantity() == 3 // 수량이 3개로 누적되어야 함
		));
	}

	@Test
	@DisplayName("다른 매장의 아이템을 추가하면 기존 장바구니가 초기화된다")
	void addDifferentStoreItem() {
		// given - 다른 매장의 아이템이 장바구니에 있는 상태
		UUID otherStoreId = UUID.randomUUID();
		cartItems.add(RedisCartItem.builder().menuId(UUID.randomUUID()).storeId(otherStoreId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when - 다른 매장의 메뉴를 추가
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then - 기존 장바구니가 삭제되고 새 아이템만 남는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 && // 1개의 아이템만 남아있어야 함
				items.get(0).getStoreId().equals(storeId) // 새로운 매장 ID여야 함
		));
	}

	@Test
	@DisplayName("장바구니 아이템의 수량을 수정할 수 있다")
	void updateItem() {
		// given - 수량 1개인 아이템이 장바구니에 있는 상태
		cartItems.add(RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when - 아이템 수량을 5개로 수정
		cartService.updateCartItem(userId, menuId, 5);

		// then - 수량이 5개로 업데이트되는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.get(0).getQuantity() == 5 // 수량이 5개로 변경되어야 함
		));
	}

	@Test
	@DisplayName("장바구니에서 특정 아이템을 삭제할 수 있다")
	void removeItem() {
		// when - 특정 메뉴 아이템을 삭제
		cartService.removeCartItem(userId, menuId);

		// then - Redis 서비스의 삭제 메서드가 호출되는지 검증
		verify(cartRedisService).removeCartItem(userId, menuId);
	}

	@Test
	@DisplayName("Redis에 장바구니가 있으면 Redis에서 조회한다")
	void getFromRedis() {
		// given - Redis에 장바구니가 존재하는 상태를 모킹
		when(cartRedisService.existsCartInRedis(userId)).thenReturn(true);
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when - 장바구니를 캐시에서 조회
		List<RedisCartItem> result = cartService.getCartFromCache(userId);

		// then - Redis에서 조회한 데이터가 반환되고 DB 동기화는 수행되지 않는지 검증
		assertThat(result).isEqualTo(cartItems); // Redis 데이터가 반환되어야 함
		verify(cartRedisService, never()).saveCartToRedis(any(), any()); // DB에서 로드하지 않아야 함
	}

	@Test
	@DisplayName("Redis에 장바구니가 없으면 DB에서 로드한다")
	void getFromDb() {
		// given - Redis에 장바구니가 없는 상태를 모킹
		when(cartRedisService.existsCartInRedis(userId)).thenReturn(false);
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// DB에 있는 장바구니 데이터를 모킹
		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();
		CartItem cartItem = CartItem.builder()
			.cart(cart)
			.menu(Menu.builder().menuId(menuId).store(Store.builder().storeId(storeId).build()).build())
			.quantity(2)
			.build();

		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartId(cart.getCartId())).thenReturn(List.of(cartItem));

		// when - 장바구니를 캐시에서 조회 (내부에서 DB 로드 수행)
		List<RedisCartItem> result = cartService.getCartFromCache(userId);

		// then - DB에서 로드한 데이터가 Redis에 저장되고 반환되는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), any()); // DB 데이터가 Redis에 저장되어야 함
		assertThat(result).isEqualTo(cartItems); // 조회된 데이터가 반환되어야 함
	}

	@Test
	@DisplayName("장바구니의 모든 아이템을 삭제할 수 있다")
	void clearItems() {
		// when - 장바구니의 모든 아이템을 삭제
		cartService.clearCartItems(userId);

		// then - Redis 서비스의 전체 삭제 메서드가 호출되는지 검증
		verify(cartRedisService).clearCartItems(userId);
	}

	@Test
	@DisplayName("DB의 장바구니 데이터를 Redis로 로드할 수 있다")
	void loadDbToRedis() {
		// given - DB에 있는 장바구니 데이터를 모킹
		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();
		CartItem cartItem = CartItem.builder()
			.cart(cart)
			.menu(Menu.builder().menuId(menuId).store(Store.builder().storeId(storeId).build()).build())
			.quantity(2)
			.build();

		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartId(cart.getCartId())).thenReturn(List.of(cartItem));

		// when - DB 데이터를 Redis로 로드
		cartService.loadDbToRedis(userId);

		// then - DB 데이터가 올바르게 변환되어 Redis에 저장되는지 검증
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 && // 1개의 아이템이 저장되어야 함
				items.get(0).getMenuId().equals(menuId) && // 메뉴 ID가 일치해야 함
				items.get(0).getStoreId().equals(storeId) && // 매장 ID가 일치해야 함
				items.get(0).getQuantity() == 2 // 수량이 2개여야 함
		));
	}

	@Test
	@DisplayName("Redis의 장바구니 데이터를 DB에 동기화할 수 있다")
	void syncRedisToDb() {
		// given - Redis에 있는 장바구니 데이터와 DB 장바구니를 모킹
		RedisCartItem redisItem = RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(3).build();
		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();

		when(cartRedisService.getCartFromRedis(userId)).thenReturn(List.of(redisItem));
		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));

		// when - Redis 데이터를 DB에 동기화
		cartService.syncRedisToDb(userId);

		// then - 기존 DB 데이터가 삭제되고 Redis 데이터가 새로 저장되는지 검증
		verify(cartItemRepository).deleteByCart_CartId(cart.getCartId()); // 기존 장바구니 아이템 삭제
		verify(cartItemRepository).saveAll(argThat((List<CartItem> items) ->
			items.size() == 1 && // 1개의 아이템이 저장되어야 함
				items.get(0).getQuantity() == 3 // 수량이 3개여야 함
		));
	}

	@Test
	@DisplayName("모든 사용자의 Redis 장바구니를 DB에 동기화할 수 있다")
	void syncAllCarts() {
		// given - 여러 사용자의 Redis 장바구니 데이터를 모킹
		Set<String> cartKeys = Set.of("cart:1", "cart:2");
		when(cartRedisService.getAllCartKeys()).thenReturn(cartKeys);
		when(cartRedisService.extractUserIdFromKey("cart:1")).thenReturn(1L);
		when(cartRedisService.extractUserIdFromKey("cart:2")).thenReturn(2L);

		// 각 사용자의 Redis 장바구니 아이템 모킹
		RedisCartItem redisItem1 = RedisCartItem.builder()
			.menuId(UUID.randomUUID())
			.storeId(storeId)
			.quantity(1)
			.build();
		RedisCartItem redisItem2 = RedisCartItem.builder()
			.menuId(UUID.randomUUID())
			.storeId(storeId)
			.quantity(2)
			.build();
		when(cartRedisService.getCartFromRedis(1L)).thenReturn(List.of(redisItem1));
		when(cartRedisService.getCartFromRedis(2L)).thenReturn(List.of(redisItem2));

		// 각 사용자의 DB 장바구니 모킹
		Cart cart1 = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(1L).build()).build();
		Cart cart2 = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(2L).build()).build();
		when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(cart1));
		when(cartRepository.findByUser_UserId(2L)).thenReturn(Optional.of(cart2));

		// when - 모든 사용자의 장바구니를 DB에 동기화
		cartService.syncAllCartsToDb();

		// then - 모든 사용자의 기존 DB 데이터가 삭제되고 Redis 데이터가 저장되는지 검증
		verify(cartItemRepository).deleteByCart_CartId(cart1.getCartId()); // 사용자 1의 기존 데이터 삭제
		verify(cartItemRepository).deleteByCart_CartId(cart2.getCartId()); // 사용자 2의 기존 데이터 삭제
		verify(cartItemRepository, times(2)).saveAll(any()); // 2번의 저장 작업이 수행되어야 함
	}
}