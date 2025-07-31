package app.domain.store;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionController {

	private final RegionService regionService;

	@PostMapping("/code/{regionId}")
	public ResponseEntity<UUID> getRegionIdByCode(@PathVariable String regionCode) {
		UUID regionId = regionService.getRegionIdByCode(regionCode);

		if (regionId == null) {
			throw new GeneralException(ErrorStatus.REGION_NOT_FOUND);
		}

		return ResponseEntity.ok(regionId);
	}
}
