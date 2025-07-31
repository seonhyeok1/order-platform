package app.domain.store;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionService {

	private final RegionRepository regionRepository;

	public UUID getRegionIdByCode(String regionCode) {
		if (regionCode == null || regionCode.isBlank()) {
			throw new IllegalArgumentException("지역 코드는 null이거나 비어있을 수 없습니다.");
		}
		Region region = regionRepository.findByRegionCode(regionCode)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역 코드입니다."));
		return region.getRegionId();
	}
}
