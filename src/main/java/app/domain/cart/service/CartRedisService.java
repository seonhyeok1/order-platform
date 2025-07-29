package app.domain.cart.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.domain.cart.model.dto.response.RedisCartItem;

public interface CartRedisService {
	void saveCartToRedis(Long userId, List<RedisCartItem> cartItems);

	List<RedisCartItem> getCartFromRedis(Long userId);

	void removeCartItem(Long userId, UUID menuId);

	void clearCartItems(Long userId);

	boolean existsCartInRedis(Long userId);

	Set<String> getAllCartKeys();

	Long extractUserIdFromKey(String key);
}
