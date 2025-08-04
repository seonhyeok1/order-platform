package app.domain.store;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.order.model.entity.Orders;
import app.domain.order.model.repository.OrdersRepository;
import app.domain.review.model.ReviewRepository;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.domain.review.model.entity.Review;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.dto.response.StoreOrderListResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreAcceptStatus;
import app.domain.store.status.StoreErrorCode;
import app.domain.user.model.entity.User;
import app.global.SecurityUtil;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;
	private final CategoryRepository categoryRepository;
	private final MenuRepository menuRepository;
	private final ReviewRepository reviewRepository;
	private final OrdersRepository ordersRepository;
	private final SecurityUtil securityUtil;

	@Transactional
	public StoreApproveResponse createStore(StoreApproveRequest request) {

		User user = securityUtil.getCurrentUser();

		Region region = regionRepository.findById(request.getRegionId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.REGION_NOT_FOUND));

		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.CATEGORY_NOT_FOUND));

		Store store = new Store(null, user, region, category, request.getStoreName(), request.getDesc(),
			request.getAddress(), request.getPhoneNumber(), request.getMinOrderAmount(), StoreAcceptStatus.PENDING);

		Store savedStore = storeRepository.save(store);

		return new StoreApproveResponse(savedStore.getStoreId(), savedStore.getStoreAcceptStatus().name());
	}

	@Transactional
	public StoreInfoUpdateResponse updateStoreInfo(StoreInfoUpdateRequest request) {

		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUser().getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		if (request.getCategoryId() != null) {
			Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new GeneralException(StoreErrorCode.CATEGORY_NOT_FOUND));
			store.setCategory(category);
		}
		if (request.getName() != null) {
			store.setStoreName(request.getName());
		}
		if (request.getAddress() != null) {
			store.setAddress(request.getAddress());
		}
		if (request.getPhoneNumber() != null) {
			store.setPhoneNumber(request.getPhoneNumber());
		}
		if (request.getMinOrderAmount() != null) {
			store.setMinOrderAmount(request.getMinOrderAmount());
		}
		if (request.getDesc() != null) {
			store.setDescription(request.getDesc());
		}

		Store updatedStore = storeRepository.save(store);
		return new StoreInfoUpdateResponse(updatedStore.getStoreId());
	}

	@Transactional
	public void deleteStore(UUID storeId) {
		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUser().getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		store.markAsDeleted();
	}

	@Transactional(readOnly = true)
	public MenuListResponse getStoreMenuList(UUID storeId) {
		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUser().getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		List<Menu> menus = menuRepository.findByStoreAndDeletedAtIsNull(store);

		List<MenuListResponse.MenuDetail> menuDetails = menus.stream()
			.map(menu -> new MenuListResponse.MenuDetail(menu.getMenuId(), menu.getName(), menu.getPrice(),
				menu.getDescription(), menu.isHidden()))
			.collect(Collectors.toList());

		return new MenuListResponse(store.getStoreId(), menuDetails);
	}

	@Transactional(readOnly = true)
	public List<GetReviewResponse> getStoreReviewList(UUID storeId) {
		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUser().getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		List<Review> reviews = reviewRepository.findByStore(store);

		return reviews.stream()
			.map(review -> new GetReviewResponse(review.getReviewId(), review.getUser().getUsername(),
				review.getStore().getStoreName(), review.getRating(), review.getContent(), review.getCreatedAt()))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public StoreOrderListResponse getStoreOrderList(UUID storeId) {
		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		if (!store.getUser().getUserId().equals(userId)) {
			throw new GeneralException(StoreErrorCode.INVALID_USER_ROLE);
		}

		List<Orders> orders = ordersRepository.findByStore(store);

		List<StoreOrderListResponse.StoreOrderDetail> orderDetails = orders.stream()
			.map(StoreOrderListResponse.StoreOrderDetail::from)
			.collect(Collectors.toList());

		return new StoreOrderListResponse(store.getStoreId(), orderDetails);
	}
}