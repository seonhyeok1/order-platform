package app.domain.store.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import app.domain.store.service.RegionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RegionController {

	private final RegionService regionService;

	@PostMapping("/region/id")
	public ResponseEntity<UUID> getRegionIdByCode(@RequestBody String regionCode) {
		UUID regionId = regionService.getRegionIdByCode(regionCode);
		return ResponseEntity.ok(regionId);
	}
}
