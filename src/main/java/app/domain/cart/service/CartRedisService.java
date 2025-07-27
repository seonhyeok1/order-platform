package app.domain.cart.service;

import java.util.List;

import app.domain.cart.model.dto.RedisCartItem;

public interface CartRedisService {
	void saveCartToRedis(Long userId, List<RedisCartItem> cartItems);

	List<RedisCartItem> getCartFromRedis(Long userId);

	void clearCartItems(Long userId);

	boolean existsCartInRedis(Long userId);
}
