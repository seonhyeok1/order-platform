package app.domain.cart.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import app.domain.cart.model.dto.response.RedisCartItem;

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
	@DisplayName("장바구니 아이템을 Redis에 저장하고 조회할 수 있다")
	void saveAndGet() {
		// given - 테스트용 사용자 ID와 장바구니 아이템 2개를 준비
		Long userId = 1L;
		UUID menuId1 = UUID.randomUUID();
		UUID menuId2 = UUID.randomUUID();
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(menuId1)
				.quantity(2) // 첫 번째 메뉴, 수량 2개
				.build(),
			RedisCartItem.builder()
				.menuId(menuId2)
				.quantity(3) // 두 번째 메뉴, 수량 3개
				.build()
		);

		// when - 장바구니를 Redis에 저장하고 다시 조회
		cartRedisService.saveCartToRedis(userId, cartItems);
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);

		// then - 저장된 아이템이 올바르게 조회되는지 검증
		assertThat(result).hasSize(2); // 2개의 아이템이 조회되어야 함
		assertThat(result).extracting(RedisCartItem::getMenuId).containsExactlyInAnyOrder(menuId1, menuId2); // 메뉴 ID가 일치해야 함
		assertThat(result).extracting(RedisCartItem::getQuantity).containsExactlyInAnyOrder(2, 3); // 수량이 일치해야 함
	}

	@Test
	@DisplayName("장바구니에서 특정 아이템을 삭제할 수 있다")
	void delete() {
		// given - 2개의 아이템이 담긴 장바구니를 준비
		Long userId = 2L;
		UUID menuId1 = UUID.randomUUID();
		UUID menuId2 = UUID.randomUUID();
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(menuId1)
				.quantity(1) // 삭제될 아이템
				.build(),
			RedisCartItem.builder()
				.menuId(menuId2)
				.quantity(2) // 남을 아이템
				.build()
		);

		// 장바구니를 Redis에 저장
		cartRedisService.saveCartToRedis(userId, cartItems);

		// when - 첫 번째 메뉴 아이템만 삭제
		cartRedisService.removeCartItem(userId, menuId1);

		// then - 삭제 후 남은 아이템이 올바른지 검증
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);
		assertThat(result).hasSize(1); // 1개의 아이템만 남아있어야 함
		assertThat(result.get(0).getMenuId()).isEqualTo(menuId2); // 두 번째 메뉴만 남아있어야 함
	}

	@Test
	@DisplayName("장바구니의 모든 아이템을 삭제할 수 있다")
	void clear() {
		// given - 아이템이 담긴 장바구니를 준비
		Long userId = 3L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1) // 삭제될 아이템
				.build()
		);
		// 장바구니를 Redis에 저장
		cartRedisService.saveCartToRedis(userId, cartItems);

		// when - 장바구니의 모든 아이템을 삭제
		cartRedisService.clearCartItems(userId);

		// then - 장바구니가 비어있는지 검증
		List<RedisCartItem> result = cartRedisService.getCartFromRedis(userId);
		assertThat(result).isEmpty(); // 장바구니가 완전히 비어있어야 함
	}

	@Test
	@DisplayName("장바구니 존재 여부를 확인할 수 있다")
	void exists() {
		// given - 테스트용 사용자 ID와 장바구니 아이템 준비
		Long userId = 4L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when & then - 장바구니가 없을 때는 false 반환
		assertThat(cartRedisService.existsCartInRedis(userId)).isFalse();

		// when & then - 장바구니를 저장한 후에는 true 반환
		cartRedisService.saveCartToRedis(userId, cartItems);
		assertThat(cartRedisService.existsCartInRedis(userId)).isTrue();
	}

	@Test
	@DisplayName("장바구니에 TTL(만료시간)이 설정되고 만료 후 삭제되는지 확인할 수 있다")
	void ttlExpiration() throws InterruptedException {
		// given - 테스트용 장바구니 아이템 준비
		Long userId = 5L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when - 장바구니를 저장하고 TTL을 3초로 설정
		cartRedisService.saveCartToRedis(userId, cartItems);
		String key = "cart:" + userId; // Redis 키 생성
		redisTemplate.expire(key, 3, TimeUnit.SECONDS); // TTL을 3초로 설정
		Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS); // TTL 조회

		// then - TTL이 설정되어 있고 장바구니가 존재하는지 검증
		assertThat(ttl).isGreaterThan(0); // TTL이 0보다 커야 함 (만료시간이 설정됨)
		assertThat(ttl).isLessThanOrEqualTo(3); // TTL이 3초 이하여야 함
		assertThat(cartRedisService.existsCartInRedis(userId)).isTrue(); // 장바구니가 존재해야 함

		// 3초 대기 후 만료 확인
		Thread.sleep(3100); // 3.1초 대기 (여유를 둘어 확실히 만료되도록)
		assertThat(cartRedisService.existsCartInRedis(userId)).isFalse(); // 만료 후 장바구니가 삭제되어야 함
		assertThat(cartRedisService.getCartFromRedis(userId)).isEmpty(); // 조회 시 빈 리스트 반환
	}

	@Test
	@DisplayName("Redis에 저장된 모든 장바구니 키를 조회할 수 있다")
	void getAllCartKeys() {
		// given - 2명의 사용자 장바구니를 준비
		Long userId1 = 6L;
		Long userId2 = 7L;
		List<RedisCartItem> cartItems = List.of(
			RedisCartItem.builder()
				.menuId(UUID.randomUUID())
				.quantity(1)
				.build()
		);

		// when - 두 사용자의 장바구니를 저장하고 모든 키 조회
		cartRedisService.saveCartToRedis(userId1, cartItems);
		cartRedisService.saveCartToRedis(userId2, cartItems);
		Set<String> keys = cartRedisService.getAllCartKeys();

		// then - 두 개의 장바구니 키가 조회되는지 검증
		assertThat(keys).hasSize(2); // 2개의 키가 조회되어야 함
		assertThat(keys).contains("cart:" + userId1, "cart:" + userId2); // 각 사용자의 키가 포함되어야 함
	}

	@Test
	@DisplayName("Redis 키에서 사용자 ID를 추출할 수 있다")
	void extractUserIdFromKey() {
		// given - "cart:123" 형태의 Redis 키 준비
		String key = "cart:123";

		// when - 키에서 사용자 ID 추출
		Long userId = cartRedisService.extractUserIdFromKey(key);

		// then - 올바른 사용자 ID가 추출되는지 검증
		assertThat(userId).isEqualTo(123L); // "cart:123"에서 123L이 추출되어야 함
	}
}