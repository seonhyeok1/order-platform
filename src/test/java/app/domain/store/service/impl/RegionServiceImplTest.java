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

import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

	@InjectMocks
	private RegionServiceImpl regionService;

	@Mock
	private RegionRepository regionRepository;

	@Test
	void getRegionIdByCode_success() {
		String regionCode = "1111010100";
		UUID regionId = UUID.randomUUID();

		Region region = Region.builder()
			.regionId(regionId)
			.regionCode(regionCode)
			.build();

		when(regionRepository.findByRegionCode(regionCode)).thenReturn(Optional.of(region));

		UUID result = regionService.getRegionIdByCode(regionCode);

		assertEquals(regionId, result);
	}

	@Test
	void getRegionIdByCode_notFound() {
		String regionCode = "INVALID_CODE";

		when(regionRepository.findByRegionCode(regionCode)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			regionService.getRegionIdByCode(regionCode);
		});

		assertEquals("존재하지 않는 지역 코드입니다.", exception.getMessage());
	}
}
