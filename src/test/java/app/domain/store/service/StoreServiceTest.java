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
import org.springframework.context.annotation.Import;

import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.store.model.enums.StoreAcceptStatus;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.global.config.MockSecurityConfig;
import app.global.config.SecurityConfig;

@Import({SecurityConfig.class, MockSecurityConfig.class})
@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

	@InjectMocks
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private UserRepository userRepository;

	@Nested
	@DisplayName("createStore Test")
	class CreateStoreTest {
		Long authenticatedUserId = 1L;

		@Test
		@DisplayName("Success")
		void createStoreSuccess() {
			UUID regionId = UUID.randomUUID();

			StoreApproveRequest request = new StoreApproveRequest(
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
			when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(mock(User.class)));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreApproveResponse response = storeService.createStore(authenticatedUserId, request);

			assertNotNull(response);
			assertEquals(StoreAcceptStatus.PENDING.name(), response.storeApprovalStatus());

			verify(regionRepository, times(1)).findById(regionId);
			verify(userRepository, times(1)).findById(authenticatedUserId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("Success : 선택적 필드가 null인 경우")
		void createStoreSuccessOptionalFieldsNull() {
			UUID regionId = UUID.randomUUID();

			StoreApproveRequest request = new StoreApproveRequest(
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

			when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(mock(User.class)));
			when(regionRepository.findById(regionId)).thenReturn(Optional.of(mockRegion));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreApproveResponse response = storeService.createStore(authenticatedUserId, request);

			assertNotNull(response);
			assertEquals(StoreAcceptStatus.PENDING.name(), response.storeApprovalStatus());

			verify(userRepository, times(1)).findById(authenticatedUserId);
			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("Fail : 지역이 존재하지 않음")
		void createStoreFailRegionNotFound() {
			UUID regionId = UUID.randomUUID();
			StoreApproveRequest request = new StoreApproveRequest(
				regionId,
				"가게주소",
				"가게이름",
				"가게설명",
				"010-1111-2222",
				10000L
			);

			when(regionRepository.findById(regionId)).thenReturn(Optional.empty());

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
				storeService.createStore(authenticatedUserId, request);
			});
			assertEquals("해당 region이 존재하지 않습니다.", exception.getMessage());

			verify(regionRepository, times(1)).findById(regionId);
			verify(storeRepository, never()).save(any(Store.class));
		}
	}

	@Nested
	@DisplayName("updateStoreInfo api 테스트")
	class UpdateStoreInfoTest {

		@Test
		@DisplayName("Success : Full Request")
		void updateStoreInfoSuccess() {
			UUID storeId = UUID.randomUUID();
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				storeId,
				"새 가게 이름",
				"새 주소",
				"010-2222-3333",
				5000L,
				"새 설명"
			);

			Store mockStore = Store.builder()
				.storeId(storeId)
				.storeName("기존 가게명")
				.address("기존 주소")
				.phoneNumber("010-1111-1111")
				.minOrderAmount(1000L)
				.description("기존 설명")
				.build();
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreInfoUpdateResponse response = storeService.updateStoreInfo(request);

			assertNotNull(response);
			assertEquals(storeId, response.storeId());

			verify(storeRepository, times(1)).findById(storeId);
			verify(storeRepository, times(1)).save(any(Store.class));
		}

		@Test
		@DisplayName("Success 선택적 필드 Null")
		void updateStoreInfoSuccessOptionalNull() {
			UUID storeId = UUID.randomUUID();
			StoreInfoUpdateRequest request = new StoreInfoUpdateRequest(
				storeId,
				null,
				null,
				null,
				null,
				null
			);

			Store mockStore = Store.builder()
				.storeId(storeId)
				.storeName("기존 가게")
				.address("기존 주소")
				.phoneNumber("010-0000-0000")
				.minOrderAmount(1000L)
				.description("기존 설명")
				.build();

			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
			when(storeRepository.save(any(Store.class))).thenReturn(mockStore);

			StoreInfoUpdateResponse response = storeService.updateStoreInfo(request);

			assertNotNull(response);
			assertEquals(storeId, response.storeId());
			verify(storeRepository, times(1)).findById(storeId);
			verify(storeRepository, times(1)).save(any(Store.class));

			assertEquals("기존 가게", mockStore.getStoreName());
			assertEquals("기존 주소", mockStore.getAddress());
			assertEquals("010-0000-0000", mockStore.getPhoneNumber());
			assertEquals(1000L, mockStore.getMinOrderAmount());
			assertEquals("기존 설명", mockStore.getDescription());
		}

		@Nested
		@DisplayName("deleteStore Test")
		class DeleteStoreTest {

			@Test
			@DisplayName("Success")
			void deleteStoreSuccess() {
				UUID storeId = UUID.randomUUID();
				Store mockStore = mock(Store.class);

				when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

				assertDoesNotThrow(() -> storeService.deleteStore(storeId));

				verify(storeRepository, times(1)).findById(storeId);
				verify(mockStore, times(1)).markAsDeleted();
			}

			@Test
			@DisplayName("Fail : 이미 삭제된 가게")
			void deleteStoreAlready() {
				UUID storeId = UUID.randomUUID();
				Store mockStore = mock(Store.class);

				when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
				doThrow(new IllegalArgumentException("가게 찾을 수 없음")).when(mockStore).markAsDeleted();

				IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
					storeService.deleteStore(storeId);
				});

				assertEquals("가게 찾을 수 없음", exception.getMessage());
			}
		}
	}
}