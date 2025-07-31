package app.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.store.RegionService;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

	@InjectMocks
	private RegionService regionService;

	@Mock
	private RegionRepository regionRepository;

	@Nested
	@DisplayName("getRegionIdByCode 테스트")
	class GetRegionIdByCodeTest {

		@Test
		@DisplayName("Success")
		void getRegionIdByCodeSuccess() {
			// Given
			String regionCode = "SEOUL";
			UUID expectedRegionId = UUID.randomUUID();
			Region mockRegion = Region.builder()
				.regionId(expectedRegionId)
				.regionCode(regionCode)
				.regionName("서울")
				.build();

			when(regionRepository.findByRegionCode(regionCode)).thenReturn(Optional.of(mockRegion));
			// When
			UUID actualRegionId = regionService.getRegionIdByCode(regionCode);
			// Then
			assertNotNull(actualRegionId);
			assertEquals(expectedRegionId, actualRegionId);
			verify(regionRepository, times(1)).findByRegionCode(regionCode);
		}

		@Test
		@DisplayName("Fail : invalidRegionCode")
		void getRegionIdByCodeFailNotFound() {
			// Given
			String invalidRegionCode = "INVALID";

			when(regionRepository.findByRegionCode(invalidRegionCode)).thenReturn(Optional.empty());

			// When & Then
			assertThrows(IllegalArgumentException.class, () -> {
				regionService.getRegionIdByCode(invalidRegionCode);
			}, "존재하지 않는 지역 코드입니다.");

			verify(regionRepository, times(1)).findByRegionCode(invalidRegionCode);
		}

		@Test
		@DisplayName("Fail : regionCode")
		void getRegionIdByCodeFailNullRegionCode() {
			// Given
			String nullRegionCode = null;

			// When & Then
			assertThrows(IllegalArgumentException.class, () -> {
				regionService.getRegionIdByCode(nullRegionCode);
			}, "지역 코드는 null이거나 비어있을 수 없습니다.");

			verify(regionRepository, never()).findByRegionCode(anyString());
		}
	}
}
