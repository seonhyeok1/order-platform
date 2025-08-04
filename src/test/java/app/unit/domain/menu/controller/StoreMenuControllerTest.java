package app.unit.domain.menu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.menu.StoreMenuController;
import app.domain.menu.StoreMenuService;
import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.request.MenuVisibleRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.status.StoreMenuErrorCode;
import app.domain.menu.status.StoreMenuSuccessStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
public class StoreMenuControllerTest {

	@InjectMocks
	private StoreMenuController menuController;

	@Mock
	private StoreMenuService menuService;

	private final UUID TEST_STORE_ID = UUID.randomUUID();
	private final UUID TEST_MENU_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() {
	}

	@Nested
	@DisplayName("메뉴 등록 API 테스트")
	class CreateMenuApiTest {

		@Test
		@DisplayName("성공: 메뉴 등록 - 모든 필드 포함")
		void createMenuSuccessAllFields() {
			MenuCreateRequest request = new MenuCreateRequest(TEST_STORE_ID, "테스트 메뉴", 10000L, "맛있는 메뉴");
			MenuCreateResponse expectedResponse = new MenuCreateResponse(TEST_MENU_ID, "테스트 메뉴");

			when(menuService.createMenu(any(MenuCreateRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuCreateResponse> response = menuController.createMenu(request);

			assertEquals(StoreMenuSuccessStatus.MENU_CREATED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			assertEquals(expectedResponse.getName(), response.result().getName());
			verify(menuService, times(1)).createMenu(any(MenuCreateRequest.class));
		}

		@Test
		@DisplayName("성공: 메뉴 등록 - description 필드 null")
		void createMenuSuccessWithNullDescription() {
			MenuCreateRequest request = new MenuCreateRequest(TEST_STORE_ID, "테스트 메뉴 (설명 없음)", 10000L, null);
			MenuCreateResponse expectedResponse = new MenuCreateResponse(TEST_MENU_ID, "테스트 메뉴 (설명 없음)");

			when(menuService.createMenu(any(MenuCreateRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuCreateResponse> response = menuController.createMenu(request);

			assertEquals(StoreMenuSuccessStatus.MENU_CREATED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			assertEquals(expectedResponse.getName(), response.result().getName());
			verify(menuService, times(1)).createMenu(any(MenuCreateRequest.class));
		}

		@Test
		@DisplayName("실패: 유효하지 않은 요청 (예: 메뉴 이름 누락)")
		void createMenuFailInvalidRequest() {
			MenuCreateRequest request = new MenuCreateRequest(TEST_STORE_ID, null, 10000L, null);

			when(menuService.createMenu(any(MenuCreateRequest.class)))
				.thenThrow(new GeneralException(StoreMenuErrorCode.MENU_NAME_NULL));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				menuController.createMenu(request);
			});

			assertEquals(StoreMenuErrorCode.MENU_NAME_NULL, exception.getCode());
		}
	}

	@Nested
	@DisplayName("메뉴 수정 API 테스트")
	class UpdateMenuApiTest {

		@Test
		@DisplayName("성공: 메뉴 수정 - 모든 필드 포함")
		void updateMenuSuccessAllFields() {
			MenuUpdateRequest request = new MenuUpdateRequest(TEST_MENU_ID, "수정된 메뉴", 12000L, null, null);
			MenuUpdateResponse expectedResponse = new MenuUpdateResponse(TEST_MENU_ID, "수정된 메뉴");

			when(menuService.updateMenu(any(MenuUpdateRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuUpdateResponse> response = menuController.updateMenu(request);

			assertEquals(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			assertEquals(expectedResponse.getName(), response.result().getName());
			verify(menuService, times(1)).updateMenu(any(MenuUpdateRequest.class));
		}

		@Test
		@DisplayName("성공: 메뉴 수정 - 선택적 필드 null")
		void updateMenuSuccessWithNullOptionalFields() {
			MenuUpdateRequest request = new MenuUpdateRequest(TEST_MENU_ID, null, null, null, null);
			MenuUpdateResponse expectedResponse = new MenuUpdateResponse(TEST_MENU_ID, "원래 메뉴");

			when(menuService.updateMenu(any(MenuUpdateRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuUpdateResponse> response = menuController.updateMenu(request);

			assertEquals(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			verify(menuService, times(1)).updateMenu(any(MenuUpdateRequest.class));
		}

		@Test
		@DisplayName("실패: 메뉴 없음")
		void updateMenuFailMenuNotFound() {
			MenuUpdateRequest request = new MenuUpdateRequest(TEST_MENU_ID, null, null, null, null);

			when(menuService.updateMenu(any(MenuUpdateRequest.class)))
				.thenThrow(new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				menuController.updateMenu(request);
			});

			assertEquals(StoreMenuErrorCode.MENU_NOT_FOUND, exception.getCode());
		}
	}

	@Nested
	@DisplayName("메뉴 숨김/노출 API 테스트")
	class UpdateMenuVisibilityApiTest {

		@Test
		@DisplayName("성공: 메뉴 숨김/노출")
		void updateMenuVisibilitySuccess() {
			MenuVisibleRequest request = new MenuVisibleRequest(TEST_MENU_ID, true);
			MenuUpdateResponse expectedResponse = new MenuUpdateResponse(TEST_MENU_ID, "테스트 메뉴");

			when(menuService.updateMenuVisibility(eq(TEST_MENU_ID), eq(true)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuUpdateResponse> response = menuController.updateMenuVisibility(TEST_MENU_ID, request);

			assertEquals(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			verify(menuService, times(1)).updateMenuVisibility(eq(TEST_MENU_ID), eq(true));
		}

		@Test
		@DisplayName("실패: 메뉴 없음")
		void updateMenuVisibilityFailMenuNotFound() {
			MenuVisibleRequest request = new MenuVisibleRequest(TEST_MENU_ID, true);

			when(menuService.updateMenuVisibility(eq(TEST_MENU_ID), eq(true)))
				.thenThrow(new GeneralException(StoreMenuErrorCode.MENU_NOT_FOUND));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				menuController.updateMenuVisibility(TEST_MENU_ID, request);
			});

			assertEquals(StoreMenuErrorCode.MENU_NOT_FOUND, exception.getCode());
		}
	}

	@Nested
	@DisplayName("메뉴 삭제 API 테스트")
	class DeleteMenuApiTest {

		@Test
		@DisplayName("성공: 메뉴 삭제")
		void deleteMenuSuccess() {
			MenuDeleteRequest request = new MenuDeleteRequest(TEST_MENU_ID);
			MenuDeleteResponse expectedResponse = new MenuDeleteResponse(TEST_MENU_ID, "DELETED");

			when(menuService.deleteMenu(any(MenuDeleteRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuDeleteResponse> response = menuController.deleteMenu(request);

			assertEquals(StoreMenuSuccessStatus.MENU_DELETED_SUCCESS.getCode(),
				response.code());
			assertEquals(expectedResponse.getMenuId(), response.result().getMenuId());
			assertEquals(expectedResponse.getStatus(), response.result().getStatus());
			verify(menuService, times(1)).deleteMenu(any(MenuDeleteRequest.class));
		}

		@Test
		@DisplayName("실패: 이미 삭제된 메뉴")
		void deleteMenuFailAlreadyDeleted() {
			MenuDeleteRequest request = new MenuDeleteRequest(TEST_MENU_ID);

			when(menuService.deleteMenu(any(MenuDeleteRequest.class)))
				.thenThrow(new GeneralException(StoreMenuErrorCode.MENU_ALREADY_DELETED));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				menuController.deleteMenu(request);
			});

			assertEquals(StoreMenuErrorCode.MENU_ALREADY_DELETED, exception.getCode());
		}
	}

	@Nested
	@DisplayName("메뉴 리스트 조회 API 테스트")
	class GetMenuListApiTest {

		@Test
		@DisplayName("성공: 메뉴 리스트 조회")
		void getMenuListSuccess() {
			List<MenuListResponse.MenuDetail> menuDetails = Arrays.asList(
				new MenuListResponse.MenuDetail(UUID.randomUUID(), "메뉴1", 1000L, "설명1", false),
				new MenuListResponse.MenuDetail(UUID.randomUUID(), "메뉴2", 2000L, "설명2", true)
			);
			MenuListResponse expectedResponse = new MenuListResponse(TEST_STORE_ID, menuDetails);

			when(menuService.getMenuList(any(MenuListRequest.class)))
				.thenReturn(expectedResponse);

			ApiResponse<MenuListResponse> response = menuController.getMenuList(TEST_STORE_ID.toString());

			assertEquals(StoreMenuSuccessStatus._OK.getCode(), response.code());
			assertEquals(expectedResponse.getStoreId(), response.result().getStoreId());
			assertEquals(expectedResponse.getMenus().size(), response.result().getMenus().size());
			verify(menuService, times(1)).getMenuList(any(MenuListRequest.class));
		}

		@Test
		@DisplayName("실패: 가게 없음")
		void getMenuListFailStoreNotFound() {
			when(menuService.getMenuList(any(MenuListRequest.class)))
				.thenThrow(new GeneralException(StoreMenuErrorCode.STORE_NOT_FOUND_FOR_MENU));

			GeneralException exception = assertThrows(GeneralException.class, () -> {
				menuController.getMenuList(TEST_STORE_ID.toString());
			});

			assertEquals(StoreMenuErrorCode.STORE_NOT_FOUND_FOR_MENU, exception.getCode());
		}
	}
}