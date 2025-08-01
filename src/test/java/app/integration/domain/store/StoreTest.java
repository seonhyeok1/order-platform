package app.integration.domain.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.store.StoreController;
import app.domain.store.StoreService;
import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;

@WebMvcTest(StoreController.class)
public class StoreTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private StoreService storeService;

	@Test
	@WithMockUser(username = "1")
	@DisplayName("Success: 가게 등록")
	void createStoreSuccess() throws Exception {
		StoreApproveRequest request = StoreApproveRequest.builder()
			.regionId(UUID.randomUUID())
			.categoryId(UUID.randomUUID())
			.address("광화문")
			.storeName("광화문 가게")
			.desc("가게 설명")
			.phoneNumber("01012345678")
			.minOrderAmount(10000L)
			.build();

		when(storeService.createStore(anyLong(), any(StoreApproveRequest.class)))
			.thenReturn(StoreApproveResponse.builder()
				.storeId(UUID.randomUUID())
				.storeApprovalStatus("PENDING")
				.build());

		String requestJson = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/store")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeApprovalStatus").value("PENDING"))
			.andExpect(jsonPath("$.storeId").exists());
	}

	@Test
	@WithMockUser
	@DisplayName("Success: 가게 삭제")
	void deleteStoreSuccess() throws Exception {
		UUID storeId = UUID.randomUUID();

		mockMvc.perform(delete("/store/{storeId}", storeId))
				.andExpect(status().isOk())
				.andExpect(content().string("가게 삭제가 완료되었습니다."));
	}
}


