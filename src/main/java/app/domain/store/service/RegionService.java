package app.domain.store.service;

import java.util.UUID;

public interface RegionService {
	UUID getRegionIdByCode(String regionCode);
}
