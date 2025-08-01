package app.domain.manager;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.manager.dto.response.GetCustomListResponse;
import app.domain.manager.dto.response.GetCustomerDetailResponse;
import app.domain.order.model.OrderItemRepository;
import app.domain.order.model.OrdersRepository;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.domain.order.model.entity.OrderItem;
import app.domain.order.model.entity.Orders;
import app.domain.user.UserSearchRepository;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

	private final UserRepository userRepository;
	private final UserSearchRepository userSearchRepository;
	private final UserAddressRepository userAddressRepository;
	private final OrdersRepository ordersRepository;
	private final OrderItemRepository orderItemRepository;

	public PagedResponse<GetCustomListResponse> getAllCustomer(Pageable pageable) {
		Page<GetCustomListResponse> page = userRepository.findAllByUserRole(UserRole.CUSTOMER, pageable)
			.map(GetCustomListResponse::from);

		return PagedResponse.from(page);
	}

	public GetCustomerDetailResponse getCustomerDetailById(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
		List<GetCustomerAddressListResponse> addressList = userAddressRepository.findAllByUserUserId(userId)
			.stream().map(GetCustomerAddressListResponse::from).toList();

		return GetCustomerDetailResponse.from(user, addressList);
	}

	public PagedResponse<OrderDetailResponse> getCustomerOrderListById(Long userId, Pageable pageable) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		Page<Orders> ordersPage = ordersRepository.findAllByUserAndDeliveryAddressIsNotNull(user, pageable);

		Page<OrderDetailResponse> mapped = ordersPage.map(order -> {
			List<OrderItem> orderItems = orderItemRepository.findByOrders(order);
			return OrderDetailResponse.from(order, orderItems);
		});

		return PagedResponse.from(mapped);
	}

	public PagedResponse<GetCustomListResponse> searchCustomer(String keyWord, Pageable pageable) {
		Page<User> users = userSearchRepository.searchUser(keyWord, pageable);

		Page<GetCustomListResponse> content = users.map(GetCustomListResponse::from);

		return PagedResponse.from(content);
	}

}