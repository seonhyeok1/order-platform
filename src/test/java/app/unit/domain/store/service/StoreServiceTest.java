package app.unit.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.menu.model.repository.MenuRepository;
import app.domain.order.model.entity.Orders;
import app.domain.order.model.repository.OrdersRepository;
import app.domain.order.service.OrderService;
import app.domain.review.model.ReviewRepository;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.domain.review.model.entity.Review;
import app.domain.store.StoreService;
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
import app.domain.user.model.entity.enums.UserRole;
import app.global.SecurityUtil;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private OrdersRepository ordersRepository;

	@Mock
	private OrderService orderService;

	@Mock
	private SecurityUtil securityUtil;

	private final Long TEST_USER_ID = 1L;

	@BeforeEach
	void setUp() {
		storeService = new StoreService(storeRepository, regionRepository, categoryRepository, menuRepository,
			reviewRepository, ordersRepository, orderService, securityUtil);
	}

	@Nested
	@DisplayName("createStore Test")
	class CreateStoreTest {
		UUID regionId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();

		@Test
		@DisplayName("Success")
		void createStoreSuccess() {
			StoreApproveRequest request = new StoreApproveRequest(regionId, categoryId, "가게주소", "가게이름",
				"가게설명", "010-1111-2222", 10000L);

			Region mockRegion = new Region(regionId, "test_code", "서울", false, "서울시", "서울시", "", "");
			Category mockCategory = new Category(categoryId, "한식");
			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);

			Store mockStore = new Store(UUID.randomUUID(), mockUser, mockRegion, mockCategory, request.getStoreName(),
				request.getDesc(), request.getAddress(), request.getPhoneNumber(), request.getMinOrderAmount(),
				StoreAcceptStatus.PENDING);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreApproveResponse response = storeService.createStore(request);

			assertNotNull(response);
			assertEquals(StoreAcceptStatus.PENDING.name(), response.getStoreApprovalStatus());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(regionRepository, times(1)).findById(regionId);
			verify(categoryRepository, times(1)).findById(categoryId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : 지역이 존재하지 않음")
		void createStoreFailRegionNotFound() {
			StoreApproveRequest request = new StoreApproveRequest(regionId, categoryId, "가게주소", "가게이름",
				"가게설명", "010-1111-2222", 10000L);

			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.createStore(request);
			});
			assertEquals(StoreErrorCode.REGION_NOT_FOUND, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(regionRepository, times(1)).findById(regionId);
			verify(categoryRepository, never()).findById(any(UUID.class));
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : 카테고리가 존재하지 않음")
		void createStoreFailCategoryNotFound() {
			StoreApproveRequest request = new StoreApproveRequest(regionId, categoryId, "가게주소", "가게이름",
				"가게설명", "010-1111-2222", 10000L);

			Region mockRegion = new Region(regionId, "test_code", "서울", false, "서울시", "서울시", "", "");
			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.createStore(request);
			});
			assertEquals(StoreErrorCode.CATEGORY_NOT_FOUND, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(regionRepository, times(1)).findById(regionId);
			verify(categoryRepository, times(1)).findById(categoryId);
			verify(storeRepository, never()).save(any(Store.class));
		}
	}

	@Nested
	@DisplayName("updateStoreInfo api 테스트")
	class UpdateStoreInfoTest {
		UUID storeId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();

		@Test
		@DisplayName("Success : Full Request")
		void updateStoreInfoSuccess() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(storeId, categoryId, "새 가게 이름",
				"새 주소", "010-2222-3333", 5000L, "새 설명");

			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Region mockRegion = new Region(UUID.randomUUID(), "test_code", "기존 지역", false, "기존 지역", "", "", "");
			Category oldCategory = new Category(UUID.randomUUID(), "기존 카테고리");
			Store mockStore = new Store(storeId, mockUser, mockRegion, oldCategory, "기존 가게명", "기존 설명",
				"기존 주소", "010-1111-1111", 1000L, StoreAcceptStatus.APPROVE);

			Category newCategory = new Category(categoryId, "새 카테고리");

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
			when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(newCategory));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreInfoUpdateResponse response = storeService.updateStoreInfo(request);

			assertNotNull(response);
			assertEquals(storeId, response.getStoreId());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(categoryRepository, times(1)).findById(categoryId);
			verify(storeRepository, times(1)).save(any(Store.class));

			assertEquals(request.getName(), mockStore.getStoreName());
			assertEquals(request.getAddress(), mockStore.getAddress());
			assertEquals(request.getPhoneNumber(), mockStore.getPhoneNumber());
			assertEquals(request.getMinOrderAmount(), mockStore.getMinOrderAmount());
			assertEquals(request.getDesc(), mockStore.getDescription());
			assertEquals(newCategory, mockStore.getCategory());
		}

		@Test
		@DisplayName("Success 선택적 필드 Null")
		void updateStoreInfoSuccessOptionalNull() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(storeId, categoryId, null, null, null, null,
				null);

			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Region mockRegion = new Region(UUID.randomUUID(), "test_code", "기존 지역", false, "기존 지역", "", "", "");
			Category oldCategory = new Category(UUID.randomUUID(), "기존 카테고리");
			Store mockStore = new Store(storeId, mockUser, mockRegion, oldCategory, "기존 가게명", "기존 설명",
				"기존 주소", "010-1111-1111", 1000L, StoreAcceptStatus.APPROVE);

			Category newCategory = new Category(categoryId, "새 카테고리");

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
			when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(newCategory));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreInfoUpdateResponse response = storeService.updateStoreInfo(request);

			assertNotNull(response);
			assertEquals(storeId, response.getStoreId());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(categoryRepository, times(1)).findById(categoryId);
			verify(storeRepository, times(1)).save(any(Store.class));

			assertEquals("기존 가게명", mockStore.getStoreName());
			assertEquals("기존 주소", mockStore.getAddress());
			assertEquals("010-1111-1111", mockStore.getPhoneNumber());
			assertEquals(1000L, mockStore.getMinOrderAmount());
			assertEquals("기존 설명", mockStore.getDescription());
			assertEquals(newCategory, mockStore.getCategory());
		}

		@Test
		@DisplayName("Fail : 가게를 찾을 수 없음")
		void updateStoreInfoFailStoreNotFound() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(storeId, categoryId, "새 가게 이름",
				"새 주소", "010-2222-3333", 5000L, "새 설명");

			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.updateStoreInfo(request);
			});
			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(categoryRepository, never()).findById(any(UUID.class));
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : 카테고리를 찾을 수 없음")
		void updateStoreInfoFailCategoryNotFound() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(storeId, categoryId, "새 가게 이름",
				"새 주소", "010-2222-3333", 5000L, "새 설명");

			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Region mockRegion = new Region(UUID.randomUUID(), "test_code", "서울", false, "서울시", "서울시", "", "");
			Category oldCategory = new Category(UUID.randomUUID(), "기존 카테고리");
			Store mockStore = new Store(storeId, mockUser, mockRegion, oldCategory, "기존 가게명", "기존 설명",
				"기존 주소", "010-1111-1111", 1000L, StoreAcceptStatus.APPROVE);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
			when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.updateStoreInfo(request);
			});
			assertEquals(StoreErrorCode.CATEGORY_NOT_FOUND, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(categoryRepository, times(1)).findById(categoryId);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : 가게 소유자가 아님")
		void updateStoreInfoFailNotStoreOwner() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(storeId, categoryId, "새 가게 이름",
				"새 주소", "010-2222-3333", 5000L, "새 설명");

			User mockUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.CUSTOMER);
			User storeOwner = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Region mockRegion = new Region(UUID.randomUUID(), "test_code", "기존 지역", false, "기존 지역", "", "", "");
			Category oldCategory = new Category(UUID.randomUUID(), "기존 카테고리");
			Store mockStore = new Store(storeId, storeOwner, mockRegion, oldCategory, "기존 가게명", "기존 설명",
				"기존 주소", "010-1111-1111", 1000L, StoreAcceptStatus.APPROVE);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.updateStoreInfo(request);
			});
			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(categoryRepository, never()).findById(any(UUID.class));
			verify(storeRepository, never()).save(any(Store.class));
		}

	}

	@Nested
	@DisplayName("deleteStore Test")
	class DeleteStoreTest {

		UUID storeId = UUID.randomUUID();

		@Test
		@DisplayName("Success")
		void deleteStoreSuccess() {
			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Store mockStore = mock(Store.class);
			when(mockStore.getUser()).thenReturn(mockUser);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

			assertDoesNotThrow(() -> storeService.deleteStore(storeId));

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(mockStore, times(1)).markAsDeleted();
		}

		@Test
		@DisplayName("Fail : 가게를 찾을 수 없음")
		void deleteStoreFailStoreNotFound() {
			User mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.deleteStore(storeId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(mock(Store.class), never()).markAsDeleted();
		}

		@Test
		@DisplayName("Fail : 가게 소유자가 아님")
		void deleteStoreFailNotStoreOwner() {
			User mockUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "nickname", "홍길동",
				"01098765432", UserRole.CUSTOMER);
			User storeOwner = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Store mockStore = new Store(storeId, storeOwner, mock(Region.class), mock(Category.class), "기존 가게명",
				"기존 설명",
				"기존 주소", "010-1111-1111", 1000L, StoreAcceptStatus.APPROVE);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.deleteStore(storeId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);

			assertNull(mockStore.getDeletedAt());
		}

	}

	@Nested
	@DisplayName("점주의 메뉴 목록 조회 테스트")
	class GetStoreMenuListTest {

		private UUID testStoreId;
		private User mockUser;
		private Store mockStore;

		@BeforeEach
		void setUp() {
			testStoreId = UUID.randomUUID();
			mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			mockStore = new Store(testStoreId, mockUser, mock(Region.class), mock(Category.class), "테스트 가게", null, null,
				null, 10000L, null);
		}

		@Test
		@DisplayName("성공: 메뉴 목록 조회")
		void getStoreMenuList_Success() {
			Menu menu1 = Menu.builder()
				.menuId(UUID.randomUUID())
				.name("메뉴1")
				.price(1000L)
				.description("설명1")
				.isHidden(false)
				.build();
			Menu menu2 = Menu.builder()
				.menuId(UUID.randomUUID())
				.name("메뉴2")
				.price(2000L)
				.description("설명2")
				.isHidden(true)
				.build();
			List<Menu> mockMenus = Arrays.asList(menu1, menu2);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
			when(menuRepository.findByStoreAndDeletedAtIsNull(mockStore)).thenReturn(mockMenus);

			MenuListResponse response = storeService.getStoreMenuList(testStoreId);

			assertNotNull(response);
			assertEquals(testStoreId, response.getStoreId());
			assertEquals(2, response.getMenus().size());
			assertEquals("메뉴1", response.getMenus().get(0).getName());
			assertEquals("메뉴2", response.getMenus().get(1).getName());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(menuRepository, times(1)).findByStoreAndDeletedAtIsNull(mockStore);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreMenuList_Fail_StoreNotFound() {
			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreMenuList(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(menuRepository, never()).findByStoreAndDeletedAtIsNull(any(Store.class));
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreMenuList_Fail_Unauthorized() {
			User anotherUser = new User(TEST_USER_ID + 1, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.CUSTOMER);

			when(securityUtil.getCurrentUser()).thenReturn(anotherUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreMenuList(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(menuRepository, never()).findByStoreAndDeletedAtIsNull(any(Store.class));
		}
	}

	@Nested
	@DisplayName("점주의 리뷰 목록 조회 테스트")
	class GetStoreReviewListTest {

		private UUID testStoreId;
		private User mockUser;
		private Store mockStore;

		@BeforeEach
		void setUp() {
			testStoreId = UUID.randomUUID();
			mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			mockStore = new Store(testStoreId, mockUser, mock(Region.class), mock(Category.class), "테스트 가게", null, null,
				null, 10000L, null);
		}

		@Test
		@DisplayName("성공: 리뷰 목록 조회")
		void getStoreReviewList_Success() {
			Review review1 = Review.builder()
				.reviewId(UUID.randomUUID())
				.user(mockUser)
				.store(mockStore)
				.rating(5L)
				.content("맛있어요")
				.build();
			Review review2 = Review.builder()
				.reviewId(UUID.randomUUID())
				.user(mockUser)
				.store(mockStore)
				.rating(4L)
				.content("좋아요")
				.build();
			List<Review> mockReviews = Arrays.asList(review1, review2);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
			when(reviewRepository.findByStore(mockStore)).thenReturn(mockReviews);

			List<GetReviewResponse> response = storeService.getStoreReviewList(testStoreId);

			assertNotNull(response);
			assertEquals(2, response.size());
			assertEquals(review1.getReviewId(), response.get(0).getReviewId());
			assertEquals(review2.getReviewId(), response.get(1).getReviewId());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(reviewRepository, times(1)).findByStore(mockStore);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreReviewList_Fail_StoreNotFound() {
			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreReviewList(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(reviewRepository, never()).findByStore(any(Store.class));
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreReviewList_Fail_Unauthorized() {
			User anotherUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "nickname",
				"홍길동",
				"01012345678", UserRole.CUSTOMER);

			when(securityUtil.getCurrentUser()).thenReturn(anotherUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreReviewList(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(reviewRepository, never()).findByStore(any(Store.class));
		}
	}

	@Nested
	@DisplayName("점주의 주문 목록 조회 테스트")
	class GetStoreOrderListTest {

		private UUID testStoreId;
		private User mockUser;
		private Store mockStore;

		@BeforeEach
		void setUp() {
			testStoreId = UUID.randomUUID();
			mockUser = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			mockStore = new Store(testStoreId, mockUser, mock(Region.class), mock(Category.class), "테스트 가게", null, null,
				null, 10000L, null);
		}

		@Test
		@DisplayName("성공: 주문 목록 조회")
		void getStoreOrderList_Success() {
			Orders order1 = Orders.builder()
				.ordersId(UUID.randomUUID())
				.user(mockUser)
				.store(mockStore)
				.totalPrice(10000L)
				.orderStatus(app.domain.order.model.entity.enums.OrderStatus.COMPLETED)
				.build();
			Orders order2 = Orders.builder()
				.ordersId(UUID.randomUUID())
				.user(mockUser)
				.store(mockStore)
				.totalPrice(20000L)
				.orderStatus(app.domain.order.model.entity.enums.OrderStatus.PENDING)
				.build();
			List<Orders> mockOrders = Arrays.asList(order1, order2);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));
			when(ordersRepository.findByStore(mockStore)).thenReturn(mockOrders);

			StoreOrderListResponse response = storeService.getStoreOrderList(testStoreId);

			assertNotNull(response);
			assertEquals(testStoreId, response.getStoreId());
			assertEquals(2, response.getOrderList().size());
			assertEquals(order1.getOrdersId(), response.getOrderList().get(0).getOrderId());
			assertEquals(order2.getOrdersId(), response.getOrderList().get(1).getOrderId());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(ordersRepository, times(1)).findByStore(mockStore);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreOrderList_Fail_StoreNotFound() {
			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreOrderList(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(ordersRepository, never()).findByStore(any(Store.class));
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreOrderList_Fail_Unauthorized() {
			User anotherUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "nickname",
				"홍길동",
				"01012345678", UserRole.CUSTOMER);

			when(securityUtil.getCurrentUser()).thenReturn(anotherUser);
			when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.getStoreOrderList(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());
			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(testStoreId);
			verify(ordersRepository, never()).findByStore(any(Store.class));
		}
	}

	@Nested
	@DisplayName("주문 수락/거절 테스트")
	class OrderAcceptRejectTest {

		private User currentUser;
		private Store store;
		private Orders order;
		private UUID orderId;
		private UUID storeId;

		@BeforeEach
		void setUp() {
			currentUser = User.builder().userId(TEST_USER_ID).username("owner").build();
			storeId = UUID.randomUUID();
			store = Store.builder().storeId(storeId).user(currentUser).build();
			orderId = UUID.randomUUID();
			order = Orders.builder().ordersId(orderId).store(store).build();

			when(securityUtil.getCurrentUser()).thenReturn(currentUser);
		}

		@Test
		@DisplayName("주문 수락 성공")
		void acceptOrder_success() {
			when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
			when(orderService.updateOrderStatus(eq(orderId), eq(app.domain.order.model.entity.enums.OrderStatus.ACCEPTED)))
				.thenReturn(mock(app.domain.order.model.dto.response.UpdateOrderStatusResponse.class));

			assertDoesNotThrow(() -> storeService.acceptOrder(orderId));
			verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(app.domain.order.model.entity.enums.OrderStatus.ACCEPTED));
		}

		@Test
		@DisplayName("주문 수락 실패 - 주문을 찾을 수 없음")
		void acceptOrder_orderNotFound() {
			when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> storeService.acceptOrder(orderId));
			assertEquals(StoreErrorCode.ORDER_NOT_FOUND, exception.getCode());
		}

		@Test
		@DisplayName("주문 수락 실패 - 현재 사용자가 가게 점주가 아님")
		void acceptOrder_notStoreOwner() {
			User anotherUser = User.builder().userId(TEST_USER_ID + 1).username("another").build();
			Store anotherStore = Store.builder().storeId(UUID.randomUUID()).user(anotherUser).build();
			Orders orderOfAnotherStore = Orders.builder().ordersId(orderId).store(anotherStore).build();

			when(ordersRepository.findById(orderId)).thenReturn(Optional.of(orderOfAnotherStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> storeService.acceptOrder(orderId));
			assertEquals(StoreErrorCode.NOT_STORE_OWNER, exception.getCode());
		}

		@Test
		@DisplayName("주문 거절 성공")
		void rejectOrder_success() {
			when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
			when(orderService.updateOrderStatus(eq(orderId), eq(app.domain.order.model.entity.enums.OrderStatus.REJECTED)))
				.thenReturn(mock(app.domain.order.model.dto.response.UpdateOrderStatusResponse.class));

			assertDoesNotThrow(() -> storeService.rejectOrder(orderId));
			verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(app.domain.order.model.entity.enums.OrderStatus.REJECTED));
		}

		@Test
		@DisplayName("주문 거절 실패 - 주문을 찾을 수 없음")
		void rejectOrder_orderNotFound() {
			when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () -> storeService.rejectOrder(orderId));
			assertEquals(StoreErrorCode.ORDER_NOT_FOUND, exception.getCode());
		}

		@Test
		@DisplayName("주문 거절 실패 - 현재 사용자가 가게 점주가 아님")
		void rejectOrder_notStoreOwner() {
			User anotherUser = User.builder().userId(TEST_USER_ID + 1).username("another").build();
			Store anotherStore = Store.builder().storeId(UUID.randomUUID()).user(anotherUser).build();
			Orders orderOfAnotherStore = Orders.builder().ordersId(orderId).store(anotherStore).build();

			when(ordersRepository.findById(orderId)).thenReturn(Optional.of(orderOfAnotherStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> storeService.rejectOrder(orderId));
			assertEquals(StoreErrorCode.NOT_STORE_OWNER, exception.getCode());
		}
	}
}