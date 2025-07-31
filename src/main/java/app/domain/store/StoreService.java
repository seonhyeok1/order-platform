package app.domain.store;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.store.model.enums.StoreAcceptStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;

	@Transactional
	public StoreApproveResponse createStore(StoreApproveRequest request) {
		if (request.regionId() == null) {
			throw new IllegalArgumentException("regionId는 null일 수 없습니다.");
		}
		Region region = regionRepository.findById(request.regionId())
			.orElseThrow(() -> new IllegalArgumentException("해당 region이 존재하지 않습니다."));

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

		if (storeRepository.existsByStoreNameAndRegion(request.storeName(), region)) {
			throw new IllegalArgumentException("지역, 가게명 중복");
		}

		Store store = Store.builder()
			.storeName(request.storeName())
			.region(region)
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
}
