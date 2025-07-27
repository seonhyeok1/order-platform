package app.domain.cart.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

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
	void RedisSave() {
		// given
		Long userId = 1L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(2)
				.build(),
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(3)
				.build()
		);

		// when
		cartRedisService.saveCartToRedis(userId, cartItems);

		// then
		List<RedisCartItem> savedItems = cartRedisService.getCartFromRedis(userId);
		assertThat(savedItems).hasSize(2);
		assertThat(savedItems.get(0).getQuantity()).isEqualTo(2);
		assertThat(savedItems.get(1).getQuantity()).isEqualTo(3);
	}

	@Test
	void RedisGet() {
		// given
		Long userId = 2L;
		UUID menuId = UUID.randomUUID();
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(menuId)
				.quantity(5)
				.build()
		);
		cartRedisService.saveCartToRedis(userId, cartItems);

		// when
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getMenuId()).isEqualTo(menuId);
		assertThat(result.get(0).getQuantity()).isEqualTo(5);
	}

	@Test
	void RedisDelete() {
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
}