package app.unit.domain.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.repository.RegionRepository;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;
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

			mockMvc.perform(post("/api/store")
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

			mockMvc.perform(put("/api/store")
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

			mockMvc.perform(delete("/api/store/{storeId}", storeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getCode()))
				.andExpect(jsonPath("$.message").value(StoreSuccessStatus.STORE_DELETED_SUCCESS.getMessage()))
				.andExpect(jsonPath("$.result").value("가게 삭제가 완료되었습니다."));

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
}