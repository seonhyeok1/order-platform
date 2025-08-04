package app.unit.domain.store.service;

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

import app.domain.menu.model.entity.Category;
import app.domain.menu.model.repository.CategoryRepository;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
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

	@InjectMocks
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private SecurityUtil securityUtil;

	private final Long TEST_USER_ID = 1L;

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
			Region mockRegion = new Region(UUID.randomUUID(), "test_code", "기존 지역", false, "기존 지역", "", "", "");
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

			User mockUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "othernick",
				"다른사람", "01098765432", UserRole.CUSTOMER);
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
			User mockUser = new User(TEST_USER_ID + 1, "otheruser", "other@example.com", "password", "othernick",
				"다른사람", "01098765432", UserRole.CUSTOMER);
			User storeOwner = new User(TEST_USER_ID, "testuser", "test@example.com", "password", "nickname", "홍길동",
				"01012345678", UserRole.OWNER);
			Store mockStore = mock(Store.class);
			when(mockStore.getUser()).thenReturn(storeOwner);

			when(securityUtil.getCurrentUser()).thenReturn(mockUser);
			when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				storeService.deleteStore(storeId);
			});
			assertEquals(StoreErrorCode.INVALID_USER_ROLE, exception.getCode());

			verify(securityUtil, times(1)).getCurrentUser();
			verify(storeRepository, times(1)).findById(storeId);
			verify(mockStore, never()).markAsDeleted();
		}
	}
}
