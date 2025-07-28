package app.domain.cart.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import app.domain.cart.model.dto.RedisCartItem;

@SpringBootTest
class CartRedisServiceTest {

	@Autowired
	private CartRedisService cartRedisService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@AfterEach
	void cleanup() {
		redisTemplate.getConnectionFactory().getConnection().flushAll();
	}

	@Test
	void saveAndGet() {
		// given
		Long userId = 1L;
		UUID menuId1 = UUID.randomUUID();
		UUID menuId2 = UUID.randomUUID();
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(menuId1)
				.quantity(2)
				.build(),
			RedisCartItem.builder()
				.menuId(menuId2)
				.quantity(3)
				.build()
		);

		// when
		cartRedisService.saveCartToRedis(userId, cartItems);
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting(RedisCartItem::getMenuId).containsExactlyInAnyOrder(menuId1, menuId2);
		assertThat(result).extracting(RedisCartItem::getQuantity).containsExactlyInAnyOrder(2, 3);
	}

	@Test
	void delete() {
		// given
		Long userId = 2L;
		UUID menuId1 = UUID.randomUUID();
		UUID menuId2 = UUID.randomUUID();
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(menuId1)
				.quantity(1)
				.build(),
			RedisCartItem.builder()
				.menuId(menuId2)
				.quantity(2)
				.build()
		);

		cartRedisService.saveCartToRedis(userId, cartItems);

		// when - 하나의 메뉴만 삭제
		cartRedisService.removeCartItem(userId, menuId1);

		// then
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getMenuId()).isEqualTo(menuId2);
	}

	@Test
	void clear() {
		// given
		Long userId = 3L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);
		cartRedisService.saveCartToRedis(userId, cartItems);

		// when
		cartRedisService.clearCartItems(userId);

		// then
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);
		assertThat(result).isEmpty();
	}

	@Test
	void exists() {
		// given
		Long userId = 4L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when & then - 장바구니가 없을 때
		assertThat(cartRedisService.existsCartInRedis(userId)).isFalse();

		// when & then - 장바구니가 있을 때
		cartRedisService.saveCartToRedis(userId, cartItems);
		assertThat(cartRedisService.existsCartInRedis(userId)).isTrue();
	}

	@Test
	void ttlExpiration() throws InterruptedException {
		// given
		Long userId = 5L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when
		cartRedisService.saveCartToRedis(userId, cartItems);
		String key = "cart:" + userId;
		Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

		// then
		assertThat(ttl).isGreaterThan(0);
		assertThat(cartRedisService.existsCartInRedis(userId)).isTrue();
	}

	@Test
	void getAllCartKeys() {
		// given
		Long userId1 = 6L;
		Long userId2 = 7L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when
		cartRedisService.saveCartToRedis(userId1, cartItems);
		cartRedisService.saveCartToRedis(userId2, cartItems);
		Set<String> keys = cartRedisService.getAllCartKeys();

		// then
		assertThat(keys).hasSize(2);
		assertThat(keys).contains("cart:" + userId1, "cart:" + userId2);
	}

	@Test
	void extractUserIdFromKey() {
		// given
		String key = "cart:123";

		// when
		Long userId = cartRedisService.extractUserIdFromKey(key);

		// then
		assertThat(userId).isEqualTo(123L);
	}
}