package app.domain.cart.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import app.domain.cart.model.CartItemRepository;
import app.domain.cart.model.CartRepository;
import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.model.entity.Cart;
import app.domain.cart.model.entity.CartItem;
import app.domain.cart.service.CartRedisService;
import app.domain.cart.service.CartService;
import app.domain.menu.model.entity.Menu;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRedisService cartRedisService;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	public void addCartItem(Long userId, UUID menuId, UUID storeId, int quantity) {
		List<RedisCartItem> items = cartRedisService.getCartFromRedis(userId);

		if (!items.isEmpty() && !items.get(0).getStoreId().equals(storeId)) {
			items.clear();
		}

		boolean isExist = items.stream().anyMatch(i -> i.getMenuId().equals(menuId));
		if (isExist) {
			items.stream()
				.filter(item -> item.getMenuId().equals(menuId))
				.findFirst()
				.ifPresent(item -> item.setQuantity(item.getQuantity() + quantity));
		} else {
			items.add(RedisCartItem.builder()
				.menuId(menuId)
				.storeId(storeId)
				.quantity(quantity)
				.build());
		}
		cartRedisService.saveCartToRedis(userId, items);
	}

	@Override
	public void updateCartItem(Long userId, UUID menuId, int quantity) {
		List<RedisCartItem> items = cartRedisService.getCartFromRedis(userId);
		items.stream()
			.filter(item -> item.getMenuId().equals(menuId))
			.findFirst()
			.ifPresent(item -> item.setQuantity(quantity));
		cartRedisService.saveCartToRedis(userId, items);
	}

	@Override
	public void removeCartItem(Long userId, UUID menuId) {
		cartRedisService.removeCartItem(userId, menuId);
	}

	@Override
	public List<RedisCartItem> getCartFromCache(Long userId) {
		if (!cartRedisService.existsCartInRedis(userId)) {
			loadDbToRedis(userId);
		}
		return cartRedisService.getCartFromRedis(userId);
	}

	@Override
	public void clearCartItems(Long userId) {
		cartRedisService.clearCartItems(userId);
	}

	@Override
	public void loadDbToRedis(Long userId) {
		cartRepository.findByUser_UserId(userId)
			.ifPresent(cart -> {
				List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
				List<RedisCartItem> redisItems = cartItems.stream()
					.map(item -> RedisCartItem.builder()
						.menuId(item.getMenu().getMenuId())
						.storeId(item.getMenu().getStore().getStoreId())
						.quantity(item.getQuantity())
						.build())
					.toList();
				cartRedisService.saveCartToRedis(userId, redisItems);
			});
	}

	@Override
	public void syncRedisToDb(Long userId) {
		List<RedisCartItem> redisItems = cartRedisService.getCartFromRedis(userId);
		Cart cart = cartRepository.findByUser_UserId(userId)
			.orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));

		cartItemRepository.deleteByCart_CartId(cart.getCartId());

		if (!redisItems.isEmpty()) {
			List<CartItem> cartItems = redisItems.stream()
				.map(item -> CartItem.builder()
					.cart(cart)
					.menu(Menu.builder().menuId(item.getMenuId()).build())
					.quantity(item.getQuantity())
					.build())
				.toList();

			cartItemRepository.saveAll(cartItems);
		}
	}

	@Override
	@Scheduled(initialDelay = 900000, fixedRate = 900000) // 15분마다 실행
	public void syncAllCartsToDb() {
		Set<String> cartKeys = cartRedisService.getAllCartKeys();
		for (String key : cartKeys) {
			Long userId = cartRedisService.extractUserIdFromKey(key);
			try {
				syncRedisToDb(userId);
			} catch (Exception e) {
				System.err.println("Failed to sync cart for userId: " + userId + ", error: " + e.getMessage());
			}
		}
	}
}
