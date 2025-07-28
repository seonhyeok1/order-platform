package app.domain.cart.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartRedisService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartRedisServiceImpl implements CartRedisService {
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void saveCartToRedis(Long userId, List<RedisCartItem> cartItems) {
		try {
			String key = "cart:" + userId;
			redisTemplate.delete(key); // 기존 데이터 삭제
			for (RedisCartItem item : cartItems) {
				String itemJson = objectMapper.writeValueAsString(item);
				redisTemplate.opsForHash().put(key, item.getMenuId().toString(), itemJson);
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException("장바구니 Redis 저장 실패", e);
		}
	}

	@Override
	public List<RedisCartItem> getCartFromRedis(Long userId) {
		try {
			String key = "cart:" + userId;
			return redisTemplate.opsForHash().values(key).stream()
				.map(value -> {
					try {
						return objectMapper.readValue((String)value, RedisCartItem.class);
					} catch (JsonProcessingException e) {
						throw new RuntimeException("장바구니 아이템 파싱 실패", e);
					}
				})
				.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("장바구니 Redis 조회 실패", e);
		}
	}

	@Override
	public void clearCartItems(Long userId) {
		String key = "cart:" + userId;
		redisTemplate.delete(key);
	}

	public void removeCartItem(Long userId, UUID menuId) {
		String key = "cart:" + userId;
		redisTemplate.opsForHash().delete(key, menuId.toString());
	}

	@Override
	public boolean existsCartInRedis(Long userId) {
		String key = "cart:" + userId;
		return redisTemplate.hasKey(key);
	}
}
