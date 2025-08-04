package app.domain.menu;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.request.MenuVisibleRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.status.MenuSuccessStatus;
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	@PostMapping("/store/menu")
	public ApiResponse<MenuCreateResponse> createMenu(@RequestBody MenuCreateRequest request) {
		MenuCreateResponse response = menuService.createMenu(request);
		return ApiResponse.onSuccess(MenuSuccessStatus.MENU_CREATED_SUCCESS, response);
	}

	@PutMapping("/store/menu")
	public ApiResponse<MenuUpdateResponse> updateMenu(@RequestBody MenuUpdateRequest request) {
		MenuUpdateResponse response = menuService.updateMenu(request);
		return ApiResponse.onSuccess(MenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@DeleteMapping("/store/menu/delete")
	public ApiResponse<MenuDeleteResponse> deleteMenu(@RequestBody MenuDeleteRequest request) {
		MenuDeleteResponse response = menuService.deleteMenu(request);
		return ApiResponse.onSuccess(MenuSuccessStatus.MENU_DELETED_SUCCESS, response);
	}

	@PutMapping("/store/menu/{menuId}/visible")
	public ApiResponse<MenuUpdateResponse> updateMenuVisibility(@PathVariable UUID menuId,
		@RequestBody MenuVisibleRequest request) {
		MenuUpdateResponse response = menuService.updateMenuVisibility(menuId,
			request.getVisible());
		return ApiResponse.onSuccess(MenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@GetMapping("/store/menu")
	public ApiResponse<MenuListResponse> getMenuList(@RequestParam("storeId") String storeId) {
		MenuListRequest request = MenuListRequest.builder().storeId(UUID.fromString(storeId)).build();
		MenuListResponse response = menuService.getMenuList(request);
		return ApiResponse.onSuccess(MenuSuccessStatus._OK, response);
	}
}
