package app.domain.cart.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.cart.model.CartItemRepository;
import app.domain.cart.model.CartRepository;
import app.domain.cart.model.dto.AddCartItemRequest;
import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.model.entity.Cart;
import app.domain.cart.model.entity.CartItem;
import app.domain.menu.model.MenuRepository;
import app.domain.menu.model.entity.Menu;
import app.domain.store.repository.StoreRepository;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

	private final CartRedisService cartRedisService;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	public String addCartItem(Long userId, AddCartItemRequest request) {
		log.info("[CART] addCartItem 진입 - userId: {}, menuId: {}", userId, request.getMenuId());
		try {
			if (!menuRepository.existsById(request.getMenuId())) {
				throw new GeneralException(ErrorStatus.MENU_NOT_FOUND);
			}

			if (!storeRepository.existsById(request.getStoreId())) {
				throw new GeneralException(ErrorStatus.STORE_NOT_FOUND);
			}

			List<RedisCartItem> items = getCartFromCache(userId);

			if (!items.isEmpty() && !items.get(0).getStoreId().equals(request.getStoreId())) {
				items.clear();
			}

			boolean isExist = items.stream().anyMatch(i -> i.getMenuId().equals(request.getMenuId()));
			if (isExist) {
				items.stream()
					.filter(item -> item.getMenuId().equals(request.getMenuId()))
					.findFirst()
					.ifPresent(item -> item.setQuantity(item.getQuantity() + request.getQuantity()));
			} else {
				items.add(RedisCartItem.builder()
					.menuId(request.getMenuId())
					.storeId(request.getStoreId())
					.quantity(request.getQuantity())
					.build());
			}

			return cartRedisService.saveCartToRedis(userId, items);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 추가 실패 - userId: {}, menuId: {}", userId, request.getMenuId(), e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public String updateCartItem(Long userId, UUID menuId, int quantity) {
		log.info("[CART] updateCartItem 진입 - userId: {}, menuId: {}", userId, menuId);
		try {

			List<RedisCartItem> items = getCartFromCache(userId);

			items.stream()
				.filter(item -> item.getMenuId().equals(menuId))
				.findFirst()
				.ifPresent(item -> item.setQuantity(quantity));
			return cartRedisService.saveCartToRedis(userId, items);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 수정 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public String removeCartItem(Long userId, UUID menuId) {
		log.info("[CART] removeCartItem 진입 - userId: {}, menuId: {}", userId, menuId);
		try {
			return cartRedisService.removeCartItem(userId, menuId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 삭제 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public List<RedisCartItem> getCartFromCache(Long userId) {
		log.info("[CART] getCartFromCache 진입 - userId: {}", userId);
		try {
			if (!cartRedisService.existsCartInRedis(userId)) {
				loadDbToRedis(userId);
			}
			return cartRedisService.getCartFromRedis(userId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 조회 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	public String clearCartItems(Long userId) {
		log.info("[CART] clearCartItems 진입 - userId: {}", userId);
		try {
			return cartRedisService.clearCartItems(userId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 전체 삭제 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public String loadDbToRedis(Long userId) {
		log.info("[CART] loadDbToRedis 진입 - userId: {}", userId);
		try {
			Cart cart = cartRepository.findByUser_UserId(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CART_NOT_FOUND));

			List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
			List<RedisCartItem> redisItems = cartItems.stream()
				.map(item -> RedisCartItem.builder()
					.menuId(item.getMenu().getMenuId())
					.storeId(item.getMenu().getStore().getStoreId())
					.quantity(item.getQuantity())
					.build())
				.toList();
			cartRedisService.saveCartToRedis(userId, redisItems);
			return "사용자 " + userId + "의 장바구니가 DB에서 Redis로 성공적으로 로드되었습니다.";
		} catch (GeneralException e) {
			throw e;
		} catch (DataAccessException e) {
			log.error("DB에서 장바구니 데이터 로드 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_DB_SYNC_FAILED);
		} catch (Exception e) {
			log.error("DB에서 Redis로 장바구니 로드 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public String syncRedisToDb(Long userId) {
		log.info("[CART] syncRedisToDb 진입 - userId: {}", userId);
		try {

			List<RedisCartItem> redisItems = getCartFromCache(userId);

			Cart cart = cartRepository.findByUser_UserId(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CART_NOT_FOUND));

			cartItemRepository.deleteByCart_CartId(cart.getCartId());
			if (!redisItems.isEmpty()) {
				List<CartItem> cartItems = redisItems.stream()
					.filter(item -> {
						boolean exists = menuRepository.existsById(item.getMenuId());
						if (!exists) {
							log.warn("메뉴가 존재하지 않아 동기화에서 제외 - menuId: {}", item.getMenuId());
						}
						return exists;
					})
					.map(item -> {
						Menu menu = menuRepository.findById(item.getMenuId()).get();
						return CartItem.builder()
							.cart(cart)
							.menu(menu)
							.quantity(item.getQuantity())
							.build();
					})
					.toList();

				cartItemRepository.saveAll(cartItems);
			}
			return "사용자 " + userId + "의 장바구니가 Redis에서 DB로 성공적으로 동기화되었습니다.";
		} catch (GeneralException e) {
			throw e;
		} catch (DataAccessException e) {
			log.error("Redis에서 DB로 장바구니 동기화 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus.CART_DB_SYNC_FAILED);
		} catch (Exception e) {
			log.error("Redis에서 DB로 장바구니 동기화 중 예상치 못한 오류 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Scheduled(initialDelay = 900000, fixedRate = 900000)
	@Transactional
	public String syncAllCartsToDb() {
		log.info("[CART] syncAllCartsToDb 진입");
		try {

			Set<String> cartKeys = cartRedisService.getAllCartKeys();
			int successCount = 0;
			for (String key : cartKeys) {
				Long userId = cartRedisService.extractUserIdFromKey(key);
				try {
					syncRedisToDb(userId);
					successCount++;
				} catch (Exception e) {
					log.error("전체 장바구니 동기화 중 개별 사용자 동기화 실패 - userId: {}", userId, e);
				}
			}
			return "전체 장바구니 동기화 완료 - 성공: " + successCount + "/" + cartKeys.size();
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("전체 장바구니 동기화 실패", e);
			throw new GeneralException(ErrorStatus.CART_DB_SYNC_FAILED);
		}
	}
}
