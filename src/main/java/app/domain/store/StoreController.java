package app.domain.store;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.request.StoreInfoUpdateRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.dto.response.StoreInfoUpdateResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.StoreRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;
	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;

	@PostMapping
	public ResponseEntity<StoreApproveResponse> createStore(@RequestBody StoreApproveRequest request) {
		validateCreateStoreRequest(request);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long userId = Long.parseLong(authentication.getName());

		StoreApproveResponse response = storeService.createStore(userId, request);
		return ResponseEntity.ok(response);
	}

	// 유효성 검증 메서드 (Controller 안에 추가)
	private void validateCreateStoreRequest(StoreApproveRequest request) {
		if (request.regionId() == null) {
			throw new IllegalArgumentException("regionId는 null일 수 없습니다.");
		}
		if (!regionRepository.existsById(request.regionId())) {
			throw new IllegalArgumentException("해당 region이 존재하지 않습니다.");
		}
		if (request.address() == null) {
			throw new IllegalArgumentException("주소는 null일 수 없습니다.");
		}
		if (request.storeName() == null) {
			throw new IllegalArgumentException("가게 이름은 null일 수 없습니다.");
		}
		if (request.minOrderAmount() == null) {
			throw new IllegalArgumentException("최소 주문 금액은 null일 수 없습니다.");
		}
		if (request.minOrderAmount() < 0) {
			throw new IllegalArgumentException("최소 주문 금액 오류");
		}
		Region region = regionRepository.findById(request.regionId())
			.orElseThrow(() -> new IllegalArgumentException("해당 region이 존재하지 않습니다."));
		if (storeRepository.existsByStoreNameAndRegion(request.storeName(), region)) {
			throw new IllegalArgumentException("지역, 가게명 중복");
		}
	}

	@PutMapping("/store")
	public ResponseEntity<StoreInfoUpdateResponse> updateStore(@RequestBody StoreInfoUpdateRequest request) {
		if (request.storeId() == null) {
			throw new IllegalArgumentException("storeId error");
		}
		if (request.minOrderAmount() != null && request.minOrderAmount() < 0) {
			throw new IllegalArgumentException("minOrderAmount error");
		}

		StoreInfoUpdateResponse response = storeService.updateStoreInfo(request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{storeId}")
	public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId) {
		storeService.deleteStore(storeId);
		return ResponseEntity.noContent().build();
	}
}



