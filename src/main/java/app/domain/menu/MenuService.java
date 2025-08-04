package app.domain.menu;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.menu.model.dto.request.MenuCreateRequest;
import app.domain.menu.model.dto.request.MenuDeleteRequest;
import app.domain.menu.model.dto.request.MenuListRequest;
import app.domain.menu.model.dto.request.MenuUpdateRequest;
import app.domain.menu.model.dto.response.MenuCreateResponse;
import app.domain.menu.model.dto.response.MenuDeleteResponse;
import app.domain.menu.model.dto.response.MenuListResponse;
import app.domain.menu.model.dto.response.MenuUpdateResponse;
import app.domain.menu.model.entity.Menu;
import app.domain.menu.repository.MenuRepository;
import app.domain.menu.status.MenuErrorCode;
import app.domain.store.model.entity.Store;
import app.domain.store.repository.StoreRepository;
import app.domain.store.status.StoreErrorCode;
import app.domain.user.model.entity.User;
import app.global.SecurityUtil;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;
	private final SecurityUtil securityUtil;

	@Transactional
	public MenuCreateResponse createMenu(MenuCreateRequest request) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(MenuErrorCode.STORE_NOT_FOUND_FOR_MENU));

		if (menuRepository.existsByStoreAndNameAndDeletedAtIsNull(store, request.getName())) {
			throw new GeneralException(MenuErrorCode.MENU_NAME_DUPLICATE);
		}

		Menu menu = Menu.builder()
			.store(store)
			.name(request.getName())
			.price(request.getPrice())
			.description(request.getDescription())
			.build();

		Menu savedMenu = menuRepository.save(menu);

		return MenuCreateResponse.builder()
			.menuId(savedMenu.getMenuId())
			.name(savedMenu.getName())
			.build();
	}

	@Transactional
	public MenuUpdateResponse updateMenu(MenuUpdateRequest request) {

		User user = securityUtil.getCurrentUser();
		long userId = user.getUserId();

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(request.getMenuId())
			.orElseThrow(() -> new GeneralException(MenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUser().getUserId().equals(userId)) {
			throw new GeneralException(MenuErrorCode.MENU_NOT_FOUND);
		}

		if (request.getName() != null && !request.getName().equals(menu.getName())) {
			if (menuRepository.existsByStoreAndNameAndDeletedAtIsNull(menu.getStore(), request.getName())) {
				throw new GeneralException(MenuErrorCode.MENU_NAME_DUPLICATE);
			}
		}

		menu.update(request.getName(), request.getPrice(), request.getDescription(), request.getIsHidden());

		Menu updatedMenu = menuRepository.save(menu);

		return MenuUpdateResponse.builder()
			.menuId(updatedMenu.getMenuId())
			.name(updatedMenu.getName())
			.build();
	}

	@Transactional
	public MenuDeleteResponse deleteMenu(MenuDeleteRequest request) {

		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(request.getMenuId())
			.orElseThrow(() -> new GeneralException(MenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUser().getUserId().equals(userId)) {
			throw new GeneralException(MenuErrorCode.MENU_NOT_FOUND);
		}

		if (menu.getDeletedAt() != null) {
			throw new GeneralException(MenuErrorCode.MENU_ALREADY_DELETED);
		}

		menu.markAsDeleted();
		menuRepository.save(menu);

		return MenuDeleteResponse.builder()
			.menuId(menu.getMenuId())
			.status("DELETED")
			.build();
	}

	@Transactional
	public MenuUpdateResponse updateMenuVisibility(UUID menuId, Boolean visible) {

		User user = securityUtil.getCurrentUser();
		Long userId = user.getUserId();

		Menu menu = menuRepository.findByMenuIdAndDeletedAtIsNull(menuId)
			.orElseThrow(() -> new GeneralException(MenuErrorCode.MENU_NOT_FOUND));

		if (!menu.getStore().getUser().getUserId().equals(userId)) {
			throw new GeneralException(MenuErrorCode.MENU_NOT_FOUND);
		}

		menu.update(null, null, null, visible);
		Menu updatedMenu = menuRepository.save(menu);

		return MenuUpdateResponse.builder()
			.menuId(updatedMenu.getMenuId())
			.name(updatedMenu.getName())
			.build();
	}

	@Transactional(readOnly = true)
	public MenuListResponse getMenuList(MenuListRequest request) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GeneralException(StoreErrorCode.STORE_NOT_FOUND));

		List<Menu> menus = menuRepository.findByStoreAndDeletedAtIsNull(store);

		List<MenuListResponse.MenuDetail> menuDetails = menus.stream()
			.map(menu -> MenuListResponse.MenuDetail.builder()
				.menuId(menu.getMenuId())
				.name(menu.getName())
				.price(menu.getPrice())
				.description(menu.getDescription())
				.isHidden(menu.isHidden())
				.build())
			.collect(Collectors.toList());

		return MenuListResponse.builder()
			.storeId(store.getStoreId())
			.menus(menuDetails)
			.build();
	}
}
