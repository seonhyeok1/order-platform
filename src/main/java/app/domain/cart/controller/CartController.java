package app.domain.cart.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.cart.model.dto.request.AddCartItemRequest;
import app.domain.cart.model.dto.request.UpdateCartItemRequest;

import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	@GetMapping("/load")
	public void loadDbToRedis(@RequestParam Long userId) {
		cartService.loadDbToRedis(userId);
	}

	@PostMapping("/item")
	public void addItemToCart(@RequestParam Long userId, @RequestBody AddCartItemRequest request) {
		cartService.addCartItem(userId, request.getMenuId(), request.getStoreId(), request.getQuantity());
	}

	@PatchMapping("/item/{menuId}")
	public void updateItemInCart(@RequestParam Long userId, @PathVariable UUID menuId, @RequestBody UpdateCartItemRequest request) {
		cartService.updateCartItem(userId, menuId, request.getQuantity());
	}

	@DeleteMapping("/item/{menuId}")
	public void removeItemFromCart(@RequestParam Long userId, @PathVariable UUID menuId) {
		cartService.removeCartItem(userId, menuId);
	}

	@GetMapping()
	public List<RedisCartItem> getCart(@RequestParam Long userId) {
		return cartService.getCartFromCache(userId);
	}

	@DeleteMapping("/item")
	public void clearCart(@RequestParam Long userId) {
		cartService.clearCartItems(userId);
	}

	@PostMapping("/sync")
	public void syncRedisToDb(@RequestParam Long userId) {
		cartService.syncRedisToDb(userId);
	}
}
