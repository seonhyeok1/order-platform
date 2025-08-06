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
import app.domain.menu.status.StoreMenuSuccessStatus;
import app.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/store")

@RequiredArgsConstructor
public class StoreMenuController {

	private final StoreMenuService storeMenuService;

	@PostMapping("/menu")
	public ApiResponse<MenuCreateResponse> createMenu(@Valid @RequestBody MenuCreateRequest request) {
		MenuCreateResponse response = storeMenuService.createMenu(request);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_CREATED_SUCCESS, response);
	}

	@PutMapping("/menu")
	public ApiResponse<MenuUpdateResponse> updateMenu(@Valid @RequestBody MenuUpdateRequest request) {
		MenuUpdateResponse response = storeMenuService.updateMenu(request);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@DeleteMapping("/menu/delete")
	public ApiResponse<MenuDeleteResponse> deleteMenu(@Valid @RequestBody MenuDeleteRequest request) {
		MenuDeleteResponse response = storeMenuService.deleteMenu(request);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_DELETED_SUCCESS, response);
	}

	@PutMapping("/menu/{menuId}/visible")
	public ApiResponse<MenuUpdateResponse> updateMenuVisibility(@PathVariable UUID menuId,
		@Valid @RequestBody MenuVisibleRequest request) {
		MenuUpdateResponse response = storeMenuService.updateMenuVisibility(menuId,
			request.getVisible());
		return ApiResponse.onSuccess(StoreMenuSuccessStatus.MENU_UPDATED_SUCCESS, response);
	}

	@GetMapping("/menu")
	public ApiResponse<MenuListResponse> getMenuList(@RequestParam("storeId") String storeId) {
		MenuListRequest request = new MenuListRequest(UUID.fromString(storeId));
		MenuListResponse response = storeMenuService.getMenuList(request);
		return ApiResponse.onSuccess(StoreMenuSuccessStatus._OK, response);
	}
}
