package app.domain.menu;

import static org.springframework.data.domain.Sort.Direction.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import app.domain.menu.model.dto.response.GetMenuListResponse;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store")
@RequestMapping("/customer")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자의 메뉴관련 API")
public class MenuController {

	private final MenuService menuService;

	@PostMapping
	public ResponseEntity<MenuCreateResponse> createMenu(@RequestBody MenuCreateRequest request) {
		Long userId = getCurrentUserId();
		MenuCreateResponse response = menuService.createMenu(userId, request);
		return ResponseEntity.ok(response);
	}

	@PutMapping
	public ResponseEntity<MenuUpdateResponse> updateMenu(@RequestBody MenuUpdateRequest request) {
		Long userId = getCurrentUserId();
		MenuUpdateResponse response = menuService.updateMenu(userId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public ResponseEntity<MenuDeleteResponse> deleteMenu(@RequestBody MenuDeleteRequest request) {
		Long userId = getCurrentUserId();
		MenuDeleteResponse response = menuService.deleteMenu(userId, request);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/menu/{menuId}")
	public ResponseEntity<MenuUpdateResponse> updateMenuVisibility(@PathVariable UUID menuId,
		@RequestBody MenuVisibleRequest request) {
		Long userId = getCurrentUserId();
		MenuUpdateResponse response = menuService.updateMenuVisibility(userId, menuId, request.getVisible());
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<MenuListResponse> getMenuList(@RequestParam("storeId") String storeId) {
		MenuListRequest request = MenuListRequest.builder().storeId(UUID.fromString(storeId)).build();
		MenuListResponse response = menuService.getMenuList(request);
		return ResponseEntity.ok(response);
	@GetMapping("/{storeId}/menus")
	@Operation(
		summary = "메뉴 조회",
		description = "사용자가 가게 메뉴들을 조회 합니다.")
	public ApiResponse<PagedResponse<GetMenuListResponse>> getMenus(
		@PathVariable UUID storeId,
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
		return ApiResponse.onSuccess(menuService.getMenusByStoreId(storeId, pageable));
	}
}

	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}
		return Long.parseLong(authentication.getName());
	}
}
