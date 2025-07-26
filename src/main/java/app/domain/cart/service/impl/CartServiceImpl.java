package app.domain.cart.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartRedisService;
import app.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRedisService cartRedisService;

	public void addCartItem(Long userId, UUID menuId, int quantity) {
		List<RedisCartItem> items = cartRedisService.getCartFromRedis(userId);

		boolean isExist = items.stream().anyMatch(i -> i.getMenuId().equals(menuId));
		if (isExist) {
			items.stream()
				.filter(item -> item.getMenuId().equals(menuId))
				.findFirst()
				.ifPresent(item -> item.setQuantity(item.getQuantity() + quantity));
		} else {
			items.add(RedisCartItem.builder()
				.menuId(menuId)
				.quantity(quantity)
				.build());
		}
		cartRedisService.saveCartToRedis(userId, items);
	}

	@Override
	public void updateCartItem(Long userId, UUID menuId, int quantity) {

	}

	@Override
	public void removeCartItem(Long userId, UUID menuId) {

	}

	@Override
	public List<RedisCartItem> getCartFromCache(Long userId) {
		return List.of();
	}

	@Override
	public void clearCartCache(Long userId) {

	}

	@Override
	public void loadDbToRedis(Long userId) {

	}

	@Override
	public void syncRedisToDb(Long userId) {

	}
}
