package app.domain.cart.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.cart.model.CartItemRepository;
import app.domain.cart.model.CartRepository;
import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.model.entity.Cart;
import app.domain.cart.model.entity.CartItem;
import app.domain.cart.service.CartRedisService;
import app.domain.cart.service.CartService;
import app.domain.menu.model.entity.Menu;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

	private final CartRedisService cartRedisService;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	@Transactional
	public void addCartItem(Long userId, UUID menuId, UUID storeId, int quantity) {
		try {
			// 1. userId에 해당하는 장바구니 조회
			List<RedisCartItem> items = cartRedisService.getCartFromRedis(userId);

			// 2. 같은 매장 메뉴가 아니라면 리스트 초기화
			if (!items.isEmpty() && !items.get(0).getStoreId().equals(storeId)) {
				items.clear();
			}

			// 3.해당 메뉴가 장바구니에 이미 있었다면 개수 늘리기, 없었다면 메뉴 추가
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

			//4. 장바구니에 다시 저장
			cartRedisService.saveCartToRedis(userId, items);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 추가 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public void updateCartItem(Long userId, UUID menuId, int quantity) {
		try {

			// 1. userId에 해당하는 장바구니 조회
			List<RedisCartItem> items = cartRedisService.getCartFromRedis(userId);

			// 2. 해당 메뉴 개수 수정
			items.stream()
				.filter(item -> item.getMenuId().equals(menuId))
				.findFirst()
				.ifPresent(item -> item.setQuantity(quantity));
			cartRedisService.saveCartToRedis(userId, items);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 수정 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public void removeCartItem(Long userId, UUID menuId) {
		try {

			// 1. 해당 메뉴 장바구니에서 제거
			cartRedisService.removeCartItem(userId, menuId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 아이템 삭제 실패 - userId: {}, menuId: {}", userId, menuId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<RedisCartItem> getCartFromCache(Long userId) {
		try {

			// 1. Redis에 장바구니 존재하는지 확인, 없으면 DB에서 가져오기
			if (!cartRedisService.existsCartInRedis(userId)) {
				loadDbToRedis(userId);
			}

			// 2. userId에 해당하는 장바구니 조회
			return cartRedisService.getCartFromRedis(userId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 조회 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public void clearCartItems(Long userId) {
		try {

			// 1. 장바구니 전체 삭제
			cartRedisService.clearCartItems(userId);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("장바구니 전체 삭제 실패 - userId: {}", userId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void loadDbToRedis(Long userId) {
		try {

			// 1. userId에 해당하는 장바구니 조회 후 Redis에 저장
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

	@Override
	@Transactional
	public void syncRedisToDb(Long userId) {
		try {

			// 1. userId에 해당하는 장바구니 조회
			List<RedisCartItem> redisItems = cartRedisService.getCartFromRedis(userId);

			// 2. 장바구니 비우기
			Cart cart = cartRepository.findByUser_UserId(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CART_NOT_FOUND));
			cartItemRepository.deleteByCart_CartId(cart.getCartId());

			// 3. Redis 장바구니에 메뉴가 담겨 있다면 DB에 저장
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

	@Override
	@Scheduled(initialDelay = 900000, fixedRate = 900000) // 15분마다 실행
	public void syncAllCartsToDb() {
		try {

			// 1. Redis에 존재하는 장바구니 key
			Set<String> cartKeys = cartRedisService.getAllCartKeys();
			for (String key : cartKeys) {
				Long userId = cartRedisService.extractUserIdFromKey(key);
				try {
					syncRedisToDb(userId);
				} catch (Exception e) {
					log.error("전체 장바구니 동기화 중 개별 사용자 동기화 실패 - userId: {}", userId, e);
				}
			}
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("전체 장바구니 동기화 실패", e);
			throw new GeneralException(ErrorStatus.CART_DB_SYNC_FAILED);
		}
	}
}
