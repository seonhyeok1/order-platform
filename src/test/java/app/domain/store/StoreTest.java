package app.domain.store;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.entity.Region;
import app.domain.store.model.entity.RegionRepository;
import app.domain.store.model.entity.StoreRepository;
import app.domain.user.model.UserRepository;
import app.global.config.MockSecurityConfig;
import app.global.config.SecurityConfig;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"})
@AutoConfigureMockMvc
@Transactional
@Import({SecurityConfig.class, MockSecurityConfig.class})
public class StoreTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private RegionRepository regionRepository;

	@Autowired
	private UserRepository userRepository;

	private UUID testRegionId;

	@BeforeEach
	void setUp() {
		testRegionId = createTestRegion();
	}

	private UUID createTestRegion() {
		Region region = Region.builder()
			.regionCode("1111010600")
			.regionName("광화문")
			.fullName("서울특별시 종로구 세종로")
			.sido("서울특별시")
			.sigungu("종로구")
			.eupmyendong("세종로")
			.isActive(true)
			.build();

		regionRepository.save(region);
		return regionRepository.findById(region.getRegionId()).orElseThrow().getRegionId();
	}

	@Test
	@WithMockUser(username = "1")
	@DisplayName("Success: 가게 등록")
	void createStoreSuccess() throws Exception {
		Long authenticatedUserId = 1L;
		UUID categoryId = UUID.randomUUID();
		StoreApproveRequest request = new StoreApproveRequest(
			testRegionId,
			categoryId,
			"광화문",
			"광화문 가게",
			"가게 설명",
			"01012345678",
			10000L
		);

		String requestJson = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/store")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeApprovalStatus").value("PENDING"))
			.andExpect(jsonPath("$.storeId").exists());
	}
}

