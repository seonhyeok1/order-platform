package app.domain.store.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.store.model.type.StoreAcceptStatus;
import app.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;
	private final RegionRepository regionRepository;

	@Override
	@Transactional // savedStore .save(store)
	public StoreApproveResponse createStore(StoreApproveRequest request) {
		Region region = regionRepository.findById(request.regionId())
			.orElseThrow(() -> new IllegalArgumentException("해당 region이 존재하지 않습니다."));

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
