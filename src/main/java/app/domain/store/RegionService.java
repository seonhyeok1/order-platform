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
			return null;
		}

		return regionRepository.findByRegionCode(regionCode)
			.map(Region::getRegionId)
			.orElse(null);
	}
}