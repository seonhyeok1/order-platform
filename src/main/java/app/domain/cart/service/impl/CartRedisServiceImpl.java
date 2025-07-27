package app.domain.cart.service.impl;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
			String json = objectMapper.writeValueAsString(cartItems);
			redisTemplate.opsForValue().set(key, json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("장바구니 Redis 저장 실패", e);
		}
	}

	@Override
	public List<RedisCartItem> getCartFromRedis(Long userId) {
		try {
			String key = "cart:" + userId;
			String json = redisTemplate.opsForValue().get(key);
			return objectMapper.readValue(json, new TypeReference<List<RedisCartItem>>() {
			});
		} catch (JsonProcessingException e) {
			throw new RuntimeException("장바구니 Redis 조회 실패", e);
		}
	}

	@Override
	public void deleteCartFromRedis(Long userId) {
		if (existsCartInRedis(userId)) {
			String key = "cart:" + userId;
			redisTemplate.delete(key);
		}
	}

	@Override
	public boolean existsCartInRedis(Long userId) {
		String key = "cart:" + userId;
		return redisTemplate.hasKey(key);
	}
}
