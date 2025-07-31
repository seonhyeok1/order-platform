package app.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.store.model.enums.StoreAcceptStatus;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

	@InjectMocks
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Nested
	@DisplayName("createStore Test")
	class CreateStoreTest {

		@Test
		@DisplayName("Success")
		void createStoreSuccess() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(), // userId
				regionId, // regionId
				"가게주소", // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			Store mockStore = Store.builder()
				.storeId(UUID.randomUUID())
				.storeAcceptStatus(StoreAcceptStatus.PENDING)
				.build();

			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreApproveResponse response = storeService.createStore(request);

			assertNotNull(response);
			assertEquals(StoreAcceptStatus.PENDING.name(), response.storeApprovalStatus());
			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("Success : 선택적 필드가 null인 경우")
		void createStoreSuccessOptionalFieldsNull() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(), // userId
				regionId, // regionId
				"가게주소", // address
				"가게이름", // storeName
				null, // desc
				null, // phoneNumber
				10000L // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			Store mockStore = Store.builder()
				.storeId(UUID.randomUUID())
				.storeAcceptStatus(StoreAcceptStatus.PENDING)
				.build();

			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreApproveResponse response = storeService.createStore(request);

			assertNotNull(response);
			assertEquals(StoreAcceptStatus.PENDING.name(), response.storeApprovalStatus());
			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("NotFound RegionId")
		void createStoreFailRegionId() {
			UUID invalidRegionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				UUID.randomUUID(), // userId
				invalidRegionId, // regionId
				"가게주소", // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			when(regionRepository.findById(invalidRegionId)).thenReturn(Optional.empty());

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "해당 region이 존재하지 않습니다.");
			verify(regionRepository, times(1)).findById(invalidRegionId);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("최소 주문 금액 에러")
		void createStoreFailMinOrderAmount() {
			UUID userId = UUID.randomUUID();
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				regionId, // regionId
				"가게주소", // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				-1000L // minOrderAmount
			);

			// Mocking, regionRepository 호출 x
			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "최소 주문 금액 오류.");

			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("지역, 가게 명 중복")
		void createStoreFailDuplicateStoreNameInRegion() {
			UUID userId = UUID.randomUUID();
			UUID regionId = UUID.randomUUID();
			String storeName = "중복된 가게 이름";
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				regionId, // regionId
				"가게주소", // address
				storeName, // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.existsByStoreNameAndRegion(storeName, mockRegion)).thenReturn(true);

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "중복된 지역, 가게입니다.");
			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, times(1)).existsByStoreNameAndRegion(storeName, mockRegion);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail: RegionId Null")
		void createStoreFailNullRegionId() {
			UUID userId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				null, // regionId
				"가게주소", // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "regionId는 null일 수 없습니다.");

			verify(regionRepository, never()).findById(any(UUID.class));
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail: Address Null")
		void createStoreFailNullAddress() {
			UUID userId = UUID.randomUUID();
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				regionId, // regionId
				null, // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "주소는 null일 수 없습니다.");

			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail: NullStoreName")
		void createStoreFailNullStoreName() {
			UUID userId = UUID.randomUUID();
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				regionId, // regionId
				"가게주소", // address
				null, // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				10000L // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "가게 이름은 null일 수 없습니다.");

			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, never()).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : Null_MinOrderAmount")
		void createStoreFailNullMinOrderAmount() {
			UUID userId = UUID.randomUUID();
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				userId, // userId
				regionId, // regionId
				"가게주소", // address
				"가게이름", // storeName
				"가게설명", // desc
				"010-1111-2222", // phoneNumber
				null // minOrderAmount
			);

			Region mockRegion = Region.builder().regionId(regionId).regionName("서울").build();
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));

			assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			}, "최소 주문 금액은 null일 수 없습니다.");

			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, never()).save(any(Store.class));
		}
	}
}