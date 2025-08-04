package app.domain.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.cart.model.dto.RedisCartItem;
import app.domain.cart.service.CartService;
import app.domain.store.repository.StoreRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.menu.model.MenuRepository;
import app.domain.menu.model.entity.Menu;
import app.domain.order.model.OrderItemRepository;
import app.domain.order.model.OrdersRepository;
import app.domain.order.model.dto.request.CreateOrderRequest;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.domain.order.model.entity.OrderItem;
import app.domain.order.model.entity.Orders;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.store.model.entity.Store;

import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrdersRepository ordersRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartService cartService;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@Transactional
	public String createOrder(Long userId, CreateOrderRequest request, LocalDateTime requsetTime) {
		try {
			List<RedisCartItem> cartItems = cartService.getCartFromCache(userId);
			if (cartItems.isEmpty()) {
				throw new GeneralException(ErrorStatus.CART_NOT_FOUND);
			}

			User user = userRepository.findById(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

			UUID storeId = cartItems.get(0).getStoreId();
			boolean allSameStore = cartItems.stream().allMatch(item -> item.getStoreId().equals(storeId));
			if (!allSameStore) {
				throw new GeneralException(ErrorStatus.ORDER_DIFFERENT_STORE);
			}

			Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));

			Orders order = Orders.builder()
				.user(user)
				.store(store)
				.paymentMethod(request.paymentMethod())
				.orderChannel(request.orderChannel())
				.receiptMethod(request.receiptMethod())
				.requestMessage(request.requestMessage())
				.totalPrice(request.totalPrice())
				.orderStatus(OrderStatus.PENDING)
				.deliveryAddress(request.deliveryAddress())
				.orderHistory(String.format("{\"pending\": \"%s\"}",
					requsetTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
				.isRefundable(true)
				.build();

			Orders savedOrder = ordersRepository.save(order);

			for (RedisCartItem cartItem : cartItems) {

				Menu menu = menuRepository.findById(cartItem.getMenuId())
					.orElseThrow(() -> new GeneralException(ErrorStatus.MENU_NOT_FOUND));

				OrderItem orderItem = OrderItem.builder()
					.orders(savedOrder)
					.menuName(menu.getName())
					.price(menu.getPrice())
					.quantity(cartItem.getQuantity())
					.build();
				orderItemRepository.save(orderItem);
			}

			return savedOrder.getOrdersId() + " 가 생성되었습니다";
		} catch (IllegalArgumentException e) {
			log.error("주문 생성 실패 - 유효하지 않은 요청: {}", request, e);
			throw new GeneralException(ErrorStatus.INVALID_ORDER_REQUEST);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("주문 생성 실패 - request: {}", request, e);
			throw new GeneralException(ErrorStatus.ORDER_CREATE_FAILED);
		}
	}

	public OrderDetailResponse getOrderDetail(UUID orderId) {
		try {
			Orders order = ordersRepository.findById(orderId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

			List<OrderItem> orderItems = orderItemRepository.findByOrders(order);

			return OrderDetailResponse.from(order, orderItems);
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			log.error("주문 상세 조회 실패 - orderId: {}", orderId, e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}