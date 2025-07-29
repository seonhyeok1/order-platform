package app.domain.store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.store.model.type.StoreAcceptStatus;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {
	@InjectMocks
	private StoreServiceImpl storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private RegionRepository regionRepository;

	@Test
	void createStore_test() {
		UUID regionId = UUID.randomUUID();
		StoreApproveRequest request = new StoreApproveRequest(
			UUID.randomUUID(),
			regionId,
			"광화문",
			"피자스쿨",
			"맛있는 피자",
			"010-1234-5678",
			12000L
		);

		Region region = Region.builder()
			.regionId(regionId)
			.regionCode("101")
			.regionName("서울")
			.build();

		Store savedStore = Store.builder()
			.storeId(UUID.randomUUID())
			.storeName(request.storeName())
			.region(region)
			.address(request.address())
			.description(request.desc())
			.phoneNumber(request.phoneNumber())
			.minOrderAmount(request.minOrderAmount())
			.storeAcceptStatus(StoreAcceptStatus.PENDING)
			.build();

		when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));
		when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

		StoreApproveResponse response = storeService.createStore(request);

		assertNotNull(response);
		assertEquals(savedStore.getStoreId(), response.storeId());
		assertEquals("PENDING", response.storeApprovalStatus());
	}

	@Test
	void createStore_region_throw() {
		UUID fakeRegionId = UUID.randomUUID();
		StoreApproveRequest request = new StoreApproveRequest(
			UUID.randomUUID(),
			fakeRegionId,
			"왕십리",
			"테스트 가게",
			"테스트 입니다.",
			"010-0000-0000",
			2000L
		);
		when(regionRepository.findById(fakeRegionId)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, () -> {
				storeService.createStore(request);
			});

		assertEquals("해당 region이 존재하지 않습니다.", exception.getMessage());
	}
}