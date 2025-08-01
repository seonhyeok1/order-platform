package app.domain.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.StoreRepository;

@ExtendWith(MockitoExtension.class)
public class StoreControllerTest {

	@InjectMocks
	private StoreController storeController;

	@Mock
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Nested
	@DisplayName("Create Store Test")
	class CreateStoreTest {

		@Test
		@DisplayName("Success : 가게 등록 요청 성공")
		void CreateStoreSuccess() {
			UUID regionId = UUID.randomUUID();
			Region mockRegion = mock(Region.class);
			//Given
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(),
				regionId,
				"주소",
				"가게 명",
				"설명",
				"01012345678",
				1000L
			);
			StoreApproveResponse response = new StoreApproveResponse(UUID.randomUUID(), "PENDING");

			when(regionRepository.existsById(request.regionId())).thenReturn(false);
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.existsByStoreNameAndRegion(anyString(), any())).thenReturn(false);
			when(storeService.createStore(request)).thenReturn(response);

			//When
			ResponseEntity<StoreApproveResponse> result = storeController.createStore(request);

			//Then
			assertEquals(HttpStatus.OK, result.getStatusCode());
			assertEquals(response, result.getBody());
		}

		@Test
		@DisplayName("Fail : 유효하지 않은 지역 ID 가게 등록 요청")
		void CreateStoreInvalidRegionId() {
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(),
				null,
				"주소",
				"가게 명",
				"설명",
				"01012345678",
				10000L
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.createStore(request));
		}

		@Test
		@DisplayName("Fail : 주소 없음 ")
		void CreateStoreAddressNotFound() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(),
				regionId,
				null,
				"가게 명",
				"설명",
				"01012345678",
				10000L
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.createStore(request));
		}

		@Test
		@DisplayName("Fail : 가게 명 없음 ")
		void CreateStoreStoreNameNotFound() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(),
				regionId,
				"주소",
				null,
				"설명",
				"01012345678",
				10000L
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.createStore(request));
		}

		@Test
		@DisplayName("Fail : 최소 주문 금액 오류 ")
		void CreateStoreMinOrderAmountError() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(),
				regionId,
				"주소",
				"가게 명",
				"설명",
				"01012345678",
				-1000L
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.createStore(request));
		}
	}

	@Nested
	@DisplayName("Store Info Update Test")
	class StoreInfoUpdateTest {

		@Test
		@DisplayName("Success : Full Request")
		void StoreInfoUpdateSuccess() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				UUID.randomUUID(),
				"가게 명",
				"주소",
				"01012345678",
				1000L,
				"설명"
			);

			StoreInfoUpdateResponse response = new StoreInfoUpdateResponse(UUID.randomUUID());

			when(storeService.updateStoreInfo(request)).thenReturn(response);

			ResponseEntity<StoreInfoUpdateResponse> result = storeController.updateStore(request);

			assertEquals(HttpStatus.OK, result.getStatusCode());
			assertEquals(response, result.getBody());
		}

		@Test
		@DisplayName("Success: 선택 Null")
		void StoreInfoUpdateOptionalNull() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				UUID.randomUUID(),
				null,
				null,
				null,
				null,
				null
			);

			StoreInfoUpdateResponse response = new StoreInfoUpdateResponse(UUID.randomUUID());

			when(storeService.updateStoreInfo(request)).thenReturn(response);

			ResponseEntity<StoreInfoUpdateResponse> result = storeController.updateStore(request);

			assertEquals(HttpStatus.OK, result.getStatusCode());
			assertEquals(response, result.getBody());
		}

		@Test
		@DisplayName("Fail : 유효하지 않은 storeId")
		void StoreInfoUpdateInvalidStoreId() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				null,
				"가게 명",
				"주소",
				"01012345678",
				1000L,
				"설명"
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.updateStore(request));
		}

		@Test
		@DisplayName("Fail : 최소 주문 금액 에러")
		void StoreInfoUpdateMinOrderAmountError() {
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				UUID.randomUUID(),
				"가게 명",
				"주소",
				"01012345678",
				-1000L,
				"설명"
			);

			assertThrows(IllegalArgumentException.class, () -> storeController.updateStore(request));
		}
	}

	@Nested
	@DisplayName("DeleteStoreTest")
	class DeleteStoreTest {

		@Test
		@DisplayName("Success")
		void DeleteStoreTestSuccess() {
			UUID storeId = UUID.randomUUID();

			ResponseEntity<Void> result = storeController.deleteStore(storeId);

			assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		}
	}
}
