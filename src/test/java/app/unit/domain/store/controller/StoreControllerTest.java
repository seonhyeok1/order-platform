package app.unit.domain.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.dto.response.StoreOrderListResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
public class StoreControllerTest {

	@InjectMocks
	private StoreController storeController;

	@Mock
	private StoreService storeService;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private StoreRepository storeRepository;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	private final UUID TEST_STORE_ID = UUID.randomUUID();
	private final UUID TEST_REGION_ID = UUID.randomUUID();
	private final UUID TEST_CATEGORY_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(storeController)
			.build();
		objectMapper = new ObjectMapper();
	}

	@Nested
	@DisplayName("가게 등록 API 테스트")
	class CreateStoreTest {

		@Test
		@DisplayName("성공: 가게 등록 요청")
		void createStoreSuccess() throws Exception {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
				"테스트 설명", "01012345678", 1000L);

			StoreApproveResponse expectedResponse = new StoreApproveResponse(TEST_STORE_ID, "PENDING");

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));
			when(storeRepository.existsByStoreNameAndRegion(anyString(), any(Region.class))).thenReturn(false);
			when(storeService.createStore(any(StoreApproveRequest.class)))
				.thenReturn(expectedResponse);

			mockMvc.perform(post("/store")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_CREATED_SUCCESS.getCode()))
				.andExpect(jsonPath("$.message").value(StoreSuccessStatus.STORE_CREATED_SUCCESS.getMessage()))
				.andExpect(jsonPath("$.result.storeId").value(expectedResponse.getStoreId().toString()))
				.andExpect(jsonPath("$.result.storeApprovalStatus").value(expectedResponse.getStoreApprovalStatus()));

			verify(storeService, times(1)).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: regionId 누락")
		void createStoreFailRegionIdNull() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(null, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게", "테스트 설명",
				"01012345678", 1000L);

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.REGION_ID_NULL, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: categoryId 누락")
		void createStoreFailCategoryIdNull() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, null, "테스트 주소", "테스트 가게", "테스트 설명",
				"01012345678", 1000L);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.CATEGORY_ID_NULL, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: address 누락")
		void createStoreFailAddressNull() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, null, "테스트 가게",
				"테스트 설명",
				"01012345678", 1000L);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.ADDRESS_NULL, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: storeName 누락")
		void createStoreFailStoreNameNull() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", null,
				"테스트 설명",
				"01012345678", 1000L);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.STORE_NAME_NULL, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: minOrderAmount 누락")
		void createStoreFailMinOrderAmountNull() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
				"테스트 설명",
				"01012345678", null);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.MIN_ORDER_AMOUNT_NULL, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: 존재하지 않는 지역")
		void createStoreFailRegionNotFound() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
				"테스트 설명",
				"01012345678", 1000L);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.empty());

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.REGION_NOT_FOUND, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: 최소 주문 금액 유효하지 않음")
		void createStoreFailMinOrderAmountInvalid() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
				"테스트 설명",
				"01012345678", -100L);

			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mock(Region.class)));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.MIN_ORDER_AMOUNT_INVALID, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}

		@Test
		@DisplayName("실패: 동일 지역에 동일한 가게 이름 존재")
		void createStoreFailDuplicateStoreNameInRegion() throws GeneralException {
			StoreApproveRequest request = new StoreApproveRequest(TEST_REGION_ID, TEST_CATEGORY_ID, "테스트 주소", "테스트 가게",
				"테스트 설명",
				"01012345678", 1000L);

			Region mockRegion = mock(Region.class);
			when(regionRepository.findById(TEST_REGION_ID)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.existsByStoreNameAndRegion(request.getStoreName(), mockRegion)).thenReturn(true);

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.createStore(request)
			);
			assertEquals(StoreErrorCode.DUPLICATE_STORE_NAME_IN_REGION, exception.getCode());

			verify(storeService, never()).createStore(any(StoreApproveRequest.class));
		}
	}

	@Nested
	@DisplayName("가게 정보 수정 API 테스트")
	class UpdateStoreTest {

		@Test
		@DisplayName("성공: 가게 정보 수정")
		void updateStoreSuccess() throws Exception {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(TEST_STORE_ID, TEST_CATEGORY_ID, "새 가게 이름",
				"새 주소", "01098765432", 2000L, "새 설명");

			StoreInfoUpdateResponse expectedResponse = new StoreInfoUpdateResponse(TEST_STORE_ID);

			when(storeService.updateStoreInfo(any(StoreInfoUpdateRequest.class)))
				.thenReturn(expectedResponse);

			mockMvc.perform(put("/store")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_UPDATED_SUCCESS.getCode()))
				.andExpect(jsonPath("$.message").value(StoreSuccessStatus.STORE_UPDATED_SUCCESS.getMessage()))
				.andExpect(jsonPath("$.result.storeId").value(expectedResponse.getStoreId().toString()));

			verify(storeService, times(1)).updateStoreInfo(any(StoreInfoUpdateRequest.class));
		}

		@Test
		@DisplayName("실패: 유효하지 않은 요청 (예: storeId 누락)")
		void updateStoreFailInvalidRequest() throws GeneralException {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(null, TEST_CATEGORY_ID, "새 가게 이름",
				"새 주소", "01098765432", 2000L, "새 설명");

			when(storeService.updateStoreInfo(any(StoreInfoUpdateRequest.class)))
				.thenThrow(new GeneralException(StoreErrorCode.STORE_ID_NULL));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.updateStore(request)
			);
			assertEquals(StoreErrorCode.STORE_ID_NULL, exception.getCode());

			verify(storeService, times(1)).updateStoreInfo(any(StoreInfoUpdateRequest.class));
		}

		@Test
		@DisplayName("실패: GeneralException 발생")
		void updateStoreFailGeneralException() throws GeneralException {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(TEST_STORE_ID, TEST_CATEGORY_ID, "새 가게 이름",
				"새 주소", "01098765432", 2000L, "새 설명");

			when(storeService.updateStoreInfo(any(StoreInfoUpdateRequest.class)))
				.thenThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.updateStore(request)
			);
			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());

			verify(storeService, times(1)).updateStoreInfo(any(StoreInfoUpdateRequest.class));
		}
	}

	@Nested
	@DisplayName("가게 삭제 API 테스트")
	class DeleteStoreTest {

		UUID storeId = UUID.randomUUID();

		@Test
		@DisplayName("성공: 가게 삭제")
		void deleteStoreSuccess() throws Exception {
			UUID storeId = TEST_STORE_ID;

			doNothing().when(storeService).deleteStore(storeId);

			mockMvc.perform(delete("/store/{storeId}", storeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getCode()))
				.andExpect(jsonPath("$.message").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getMessage()))
				.andExpect(jsonPath("$.result").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getMessage()));

			verify(storeService, times(1)).deleteStore(storeId);
		}

		@Test
		@DisplayName("실패: GeneralException 발생")
		void deleteStoreFailGeneralException() throws GeneralException {
			UUID storeId = TEST_STORE_ID;

			doThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND)).when(storeService).deleteStore(storeId);

			GeneralException exception = assertThrows(GeneralException.class, () ->
				storeController.deleteStore(storeId)
			);
			assertEquals(StoreErrorCode.STORE_NOT_FOUND, exception.getCode());

			verify(storeService, times(1)).deleteStore(storeId);
		}
	}

	private <T> void assertSuccessResponse(ApiResponse<T> response, StoreSuccessStatus expectedStatus,
		T expectedResult) {
		assertNotNull(response);
		assertEquals(expectedStatus.getCode(), response.code());
		assertEquals(expectedStatus.getMessage(), response.message());
		assertEquals(expectedResult, response.result());
	}

	@Nested
	@DisplayName("점주의 메뉴 목록 조회 API 테스트")
	class GetStoreMenusTest {

		private final UUID testStoreId = TEST_STORE_ID;

		@Test
		@DisplayName("성공: 메뉴 목록 조회")
		void getStoreMenus_Success() {
			MenuListResponse.MenuDetail menuDetail = new
				MenuListResponse.MenuDetail(UUID.randomUUID(), "메뉴1", 1000L,
				"설명1", false);
			MenuListResponse expectedResponse = new
				MenuListResponse(testStoreId,
				Collections.singletonList(menuDetail));

			when(storeService.getStoreMenuList(testStoreId)).thenReturn(expectedResponse);

			ApiResponse<MenuListResponse> response =
				storeController.getStoreMenus(testStoreId);

			assertSuccessResponse(response, StoreSuccessStatus._OK,
				expectedResponse);
			verify(storeService, times(1)).getStoreMenuList(testStoreId);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreMenus_Fail_StoreNotFound() {
			when(storeService.getStoreMenuList(testStoreId))
				.thenThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreMenus(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND,
				exception.getCode());
			verify(storeService, times(1)).getStoreMenuList(testStoreId);
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreMenus_Fail_Unauthorized() {
			when(storeService.getStoreMenuList(testStoreId))
				.thenThrow(new
					GeneralException(StoreErrorCode.INVALID_USER_ROLE));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreMenus(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE,
				exception.getCode());
			verify(storeService, times(1)).getStoreMenuList(testStoreId);
		}
	}

	@Nested
	@DisplayName("점주의 리뷰 목록 조회 API 테스트")
	class GetStoreReviewsTest {

		private final UUID testStoreId = TEST_STORE_ID;

		@Test
		@DisplayName("성공: 리뷰 목록 조회")
		void getStoreReviews_Success() {
			GetReviewResponse reviewResponse = new
				GetReviewResponse(UUID.randomUUID(), "고객1", "가게1", 5L,
				"맛있어요", LocalDateTime.now());
			List<GetReviewResponse> expectedResponse =
				Collections.singletonList(reviewResponse);

			when(storeService.getStoreReviewList(testStoreId)).thenReturn(expectedResponse);

			ApiResponse<List<GetReviewResponse>> response =
				storeController.getStoreReviews(testStoreId);

			assertSuccessResponse(response, StoreSuccessStatus._OK,
				expectedResponse);
			verify(storeService, times(1)).getStoreReviewList(testStoreId);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreReviews_Fail_StoreNotFound() {
			when(storeService.getStoreReviewList(testStoreId))
				.thenThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreReviews(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND,
				exception.getCode());
			verify(storeService, times(1)).getStoreReviewList(testStoreId);
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreReviews_Fail_Unauthorized() {
			when(storeService.getStoreReviewList(testStoreId))
				.thenThrow(new
					GeneralException(StoreErrorCode.INVALID_USER_ROLE));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreReviews(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE,
				exception.getCode());
			verify(storeService, times(1)).getStoreReviewList(testStoreId);
		}
	}

	@Nested
	@DisplayName("점주의 주문 목록 조회 API 테스트")
	class GetStoreOrdersTest {

		private final UUID testStoreId = TEST_STORE_ID;

		@Test
		@DisplayName("성공: 주문 목록 조회")
		void getStoreOrders_Success() {
			StoreOrderListResponse.StoreOrderDetail orderDetail =
				StoreOrderListResponse.StoreOrderDetail.builder()
					.orderId(UUID.randomUUID())
					.customerName("고객1")
					.totalPrice(15000L)
					.orderStatus(app.domain.order.model.entity.enums.OrderStatus.COMPLETED)
					.orderedAt(LocalDateTime.now())
					.build();
			StoreOrderListResponse expectedResponse =
				StoreOrderListResponse.builder()
					.storeId(testStoreId)
					.orderList(Collections.singletonList(orderDetail))
					.build();

			when(storeService.getStoreOrderList(testStoreId)).thenReturn(expectedResponse);

			ApiResponse<StoreOrderListResponse> response =
				storeController.getStoreOrders(testStoreId);

			assertSuccessResponse(response, StoreSuccessStatus._OK,
				expectedResponse);
			verify(storeService, times(1)).getStoreOrderList(testStoreId);
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getStoreOrders_Fail_StoreNotFound() {
			when(storeService.getStoreOrderList(testStoreId))
				.thenThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreOrders(testStoreId);
			});

			assertEquals(StoreErrorCode.STORE_NOT_FOUND,
				exception.getCode());
			verify(storeService, times(1)).getStoreOrderList(testStoreId);
		}

		@Test
		@DisplayName("실패: 권한 없음")
		void getStoreOrders_Fail_Unauthorized() {
			when(storeService.getStoreOrderList(testStoreId))
				.thenThrow(new
					GeneralException(StoreErrorCode.INVALID_USER_ROLE));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.getStoreOrders(testStoreId);
			});

			assertEquals(StoreErrorCode.INVALID_USER_ROLE,
				exception.getCode());
			verify(storeService, times(1)).getStoreOrderList(testStoreId);
		}
	}

	@Nested
	@DisplayName("주문 수락/거절 테스트")
	class OrderAcceptRejectTest {
		@Test
		@DisplayName("주문 수락 API 성공 테스트")
		void acceptOrder_success() {
			UUID orderId = UUID.randomUUID();
			doNothing().when(storeService).acceptOrder(orderId);

			ApiResponse<String> response = storeController.acceptOrder(orderId);

			assertSuccessResponse(response, StoreSuccessStatus.ORDER_ACCEPTED_SUCCESS, "주문 수락이 완료되었습니다.");
			verify(storeService, times(1)).acceptOrder(orderId);
		}

		@Test
		@DisplayName("주문 거절 API 성공 테스트")
		void rejectOrder_success() {
			UUID orderId = UUID.randomUUID();
			doNothing().when(storeService).rejectOrder(orderId);

			ApiResponse<String> response = storeController.rejectOrder(orderId);

			assertSuccessResponse(response, StoreSuccessStatus.ORDER_REJECTED_SUCCESS, "주문 거절이 완료되었습니다.");
			verify(storeService, times(1)).rejectOrder(orderId);
		}

		@Test
		@DisplayName("주문 수락 API 실패 테스트 - 서비스에서 예외 발생")
		void acceptOrder_fail_throwException() {
			UUID orderId = UUID.randomUUID();
			doThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND)).when(storeService).acceptOrder(orderId);

			assertThrows(GeneralException.class, () -> storeController.acceptOrder(orderId));
		}

		@Test
		@DisplayName("주문 거절 API 실패 테스트 - 서비스에서 예외 발생")
		void rejectOrder_fail_throwException() {
			UUID orderId = UUID.randomUUID();
			doThrow(new GeneralException(StoreErrorCode.STORE_NOT_FOUND)).when(storeService).rejectOrder(orderId);

			assertThrows(GeneralException.class, () -> storeController.rejectOrder(orderId));
		}

		@Test
		@DisplayName("주문 수락 API 실패 테스트 - 권한 없음 - 서비스 layer")
		void acceptOrder_fail_unauthorized() {
			UUID orderId = UUID.randomUUID();
			doThrow(new GeneralException(StoreErrorCode.INVALID_USER_ROLE))
				.when(storeService).acceptOrder(orderId);

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.acceptOrder(orderId);
			});
			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());
		}

		@Test
		@DisplayName("주문 거절 API 실패 테스트 - 권한 없음 - 서비스 layer")
		void rejectOrder_fail_unauthorized() {
			UUID orderId = UUID.randomUUID();
			doThrow(new GeneralException(StoreErrorCode.INVALID_USER_ROLE))
				.when(storeService).rejectOrder(orderId);

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeController.rejectOrder(orderId);
			});
			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());
		}
	}
}