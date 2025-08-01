package app.domain.store;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;
	private final UserRepository userRepository;

	@Transactional
	public StoreApproveResponse createStore(Long userId, StoreApproveRequest request) {
		// Controller 검증 후, Service Layer에서 한번 더 검증
		Region region = regionRepository.findById(request.regionId())
			.orElseThrow(() -> new IllegalArgumentException("해당 region이 존재하지 않습니다."));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("해당 user 존재하지 않음"));

		Store store = Store.builder()
			.storeName(request.storeName())
			.region(region)
			.user(user)
			.address(request.address())
			.description(request.desc())
			.phoneNumber(request.phoneNumber())
			.minOrderAmount(request.minOrderAmount().intValue())
			.storeAcceptStatus(StoreAcceptStatus.PENDING)
			.build();

		Store savedStore = storeRepository.save(store);

		return new StoreApproveResponse(
			savedStore.getStoreId(),
			savedStore.getStoreAcceptStatus().name()
		);
	}

	@Transactional
	public StoreInfoUpdateResponse updateStoreInfo(StoreInfoUpdateRequest request) {
		Store store = storeRepository.findById(request.storeId())
			.orElseThrow(() -> new IllegalArgumentException("가게 찾을 수 없음"));

		if (request.name() != null) {
			store.setStoreName(request.name());
		}
		if (request.address() != null) {
			store.setAddress(request.address());
		}
		if (request.phoneNumber() != null) {
			store.setPhoneNumber(request.phoneNumber());
		}
		if (request.minOrderAmount() != null) {
			store.setMinOrderAmount(request.minOrderAmount());
		}
		if (request.desc() != null) {
			store.setDescription(request.desc());
		}

		Store updatedStore = storeRepository.save(store);
		return new StoreInfoUpdateResponse(updatedStore.getStoreId()); // update'd'
	}

	@Transactional
	public void deleteStore(UUID storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		store.markAsDeleted();
	}
}
