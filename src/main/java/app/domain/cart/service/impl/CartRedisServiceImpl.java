package app.domain.cart.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartRedisService;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartRedisServiceImpl implements CartRedisService {
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private static final Duration CART_TTL = Duration.ofMinutes(30);

	@Override
	public void saveCartToRedis(Long userId, List<RedisCartItem> cartItems) {
		try {
			String key = "cart:" + userId;
			redisTemplate.delete(key);
			for (RedisCartItem item : cartItems) {
				String itemJson = objectMapper.writeValueAsString(item);
				redisTemplate.opsForHash().put(key, item.getMenuId().toString(), itemJson);
			}
			redisTemplate.expire(key, CART_TTL);
		} catch (JsonProcessingException e) {
			log.error("장바구니 Redis 저장 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED);
		} catch (Exception e) {
			log.error("장바구니 Redis 저장 중 예상치 못한 오류 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED);
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
						log.error("장바구니 아이템 파싱 실패 - userId: {}", userId, e);
						throw new GeneralException(ErrorStatus.CART_ITEM_PARSE_FAILED);
					}
				})
				.collect(Collectors.toList());
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 Redis 조회 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_LOAD_FAILED);
		}
	}

	@Override
	public void clearCartItems(Long userId) {
		try {
			saveCartToRedis(userId, List.of());
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 전체 삭제 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED);
		}
	}

	@Override
	public void removeCartItem(Long userId, UUID menuId) {
		try {
			String key = "cart:" + userId;
			redisTemplate.opsForHash().delete(key, menuId.toString());
			redisTemplate.expire(key, CART_TTL);
		} catch (Exception e) {
			log.error("장바구니 아이템 삭제 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_SAVE_FAILED);
		}
	}

	@Override
	public boolean existsCartInRedis(Long userId) {
		try {
			String key = "cart:" + userId;
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			log.error("장바구니 존재 여부 확인 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_REDIS_LOAD_FAILED);
		}
	}

	@Override
	public Set<String> getAllCartKeys() {
		try {
			return redisTemplate.keys("cart:*");
		} catch (Exception e) {
			log.error("모든 장바구니 키 조회 실패", e);
			throw new GeneralException(ErrorStatus.CART_REDIS_LOAD_FAILED);
		}
	}

	@Override
	public Long extractUserIdFromKey(String key) {
		try {
			return Long.parseLong(key.substring(5));
		} catch (Exception e) {
			log.error("키에서 userId 추출 실패 - key: {}", key, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}
