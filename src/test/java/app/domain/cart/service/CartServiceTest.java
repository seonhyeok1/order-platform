package app.domain.cart.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.cart.model.CartItemRepository;
import app.domain.cart.model.CartRepository;
import app.domain.cart.model.dto.RedisCartItem;
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
	void addCartItem() {
		// given
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 &&
				items.get(0).getMenuId().equals(menuId) &&
				items.get(0).getQuantity() == 2
		));
	}

	@Test
	void addCartItem_shouldUpdateQuantityIfAlreadyExists() {
		// given
		cartItems.add(RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.get(0).getQuantity() == 3
		));
	}

	@Test
	void addCartItem_shouldClearCartIfDifferentStoreItemExists() {
		// given
		UUID otherStoreId = UUID.randomUUID();
		cartItems.add(RedisCartItem.builder().menuId(UUID.randomUUID()).storeId(otherStoreId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when
		cartService.addCartItem(userId, menuId, storeId, 2);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 &&
				items.get(0).getStoreId().equals(storeId)
		));
	}

	@Test
	void updateCartItem_shouldChangeQuantity() {
		// given
		cartItems.add(RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(1).build());
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when
		cartService.updateCartItem(userId, menuId, 5);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.get(0).getQuantity() == 5
		));
	}

	@Test
	void removeCartItem_shouldCallRedisRemove() {
		// when
		cartService.removeCartItem(userId, menuId);

		// then
		verify(cartRedisService).removeCartItem(userId, menuId);
	}

	@Test
	void getCartFromCache_shouldReturnFromRedisIfExists() {
		// given
		when(cartRedisService.existsCartInRedis(userId)).thenReturn(true);
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		// when
		List<RedisCartItem> result = cartService.getCartFromCache(userId);

		// then
		assertThat(result).isEqualTo(cartItems);
		verify(cartRedisService, never()).saveCartToRedis(any(), any());
	}

	@Test
	void getCartFromCache_shouldLoadFromDbIfRedisNotExists() {
		// given
		when(cartRedisService.existsCartInRedis(userId)).thenReturn(false);
		when(cartRedisService.getCartFromRedis(userId)).thenReturn(cartItems);

		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();
		CartItem cartItem = CartItem.builder()
			.cart(cart)
			.menu(Menu.builder().menuId(menuId).store(Store.builder().storeId(storeId).build()).build())
			.quantity(2)
			.build();

		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartId(cart.getCartId())).thenReturn(List.of(cartItem));

		// when
		List<RedisCartItem> result = cartService.getCartFromCache(userId);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), any());
		assertThat(result).isEqualTo(cartItems);
	}

	@Test
	void clearCartItems_shouldCallRedisClear() {
		// when
		cartService.clearCartItems(userId);

		// then
		verify(cartRedisService).clearCartItems(userId);
	}

	@Test
	void loadDbToRedis_shouldSaveConvertedCartToRedis() {
		// given
		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();
		CartItem cartItem = CartItem.builder()
			.cart(cart)
			.menu(Menu.builder().menuId(menuId).store(Store.builder().storeId(storeId).build()).build())
			.quantity(2)
			.build();

		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCart_CartId(cart.getCartId())).thenReturn(List.of(cartItem));

		// when
		cartService.loadDbToRedis(userId);

		// then
		verify(cartRedisService).saveCartToRedis(eq(userId), argThat(items ->
			items.size() == 1 &&
				items.get(0).getMenuId().equals(menuId) &&
				items.get(0).getStoreId().equals(storeId) &&
				items.get(0).getQuantity() == 2
		));
	}

	@Test
	void syncRedisToDb_shouldUpdateDatabaseWithRedisCart() {
		// given
		RedisCartItem redisItem = RedisCartItem.builder().menuId(menuId).storeId(storeId).quantity(3).build();
		Cart cart = Cart.builder().cartId(UUID.randomUUID()).user(User.builder().userId(userId).build()).build();

		when(cartRedisService.getCartFromRedis(userId)).thenReturn(List.of(redisItem));
		when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(cart));

		// when
		cartService.syncRedisToDb(userId);

		// then
		verify(cartItemRepository).deleteByCart_CartId(cart.getCartId());
		verify(cartItemRepository).saveAll(argThat((List<CartItem> items) ->
			items.size() == 1 &&
				items.get(0).getQuantity() == 3
		));
	}
}