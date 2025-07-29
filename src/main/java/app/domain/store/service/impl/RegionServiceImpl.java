package app.domain.store.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.service.RegionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

	private final RegionRepository regionRepository;

	@Override
	public UUID getRegionIdByCode(String regionCode) {
		Region region = regionRepository.findByRegionCode(regionCode)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역 코드입니다."));
		return region.getRegionId();
	}
}
