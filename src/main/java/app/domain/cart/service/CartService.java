package app.domain.cart.service;

import java.util.List;
import java.util.UUID;

import app.domain.cart.model.dto.response.RedisCartItem;

public interface CartService {
	void addCartItem(Long userId, UUID menuId, UUID storeId, int quantity);

	void updateCartItem(Long userId, UUID menuId, int quantity);

	void removeCartItem(Long userId, UUID menuId);

	List<RedisCartItem> getCartFromCache(Long userId);

	void clearCartItems(Long userId);

	void loadDbToRedis(Long userId);

	void syncRedisToDb(Long userId);

	void syncAllCartsToDb();
}
