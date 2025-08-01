package app.domain.store;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.StoreRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StoreTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private RegionRepository regionRepository;

	private UUID createTestRegion() {
		Region region = Region.builder()
			.regionCode("1111010600")
			.regionName("광화문")
			.build();

		regionRepository.save(region);
		return region.getRegionId();
	}
}
