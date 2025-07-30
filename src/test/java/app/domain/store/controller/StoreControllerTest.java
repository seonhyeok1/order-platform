package app.domain.store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.owner.model.dto.request.StoreApproveRequest;
import app.domain.owner.model.dto.response.StoreApproveResponse;
import app.domain.store.service.StoreService;

@SpringBootTest
@AutoConfigureMockMvc
class StoreControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private StoreService storeService;

	@TestConfiguration
	static class MockServiceConfig {
		@Bean
		public StoreService storeService() {
			return Mockito.mock(StoreService.class);
		}
	}

	@Test
	void createStore_success() throws Exception {
		UUID storeId = UUID.randomUUID();
		StoreApproveRequest request = new StoreApproveRequest(
			UUID.randomUUID(),
			UUID.randomUUID(),
			"광화문",
			"피자 스쿨",
			"맛있는 피자",
			"010-1234-5678",
			12000L
		);

		StoreApproveResponse response = new StoreApproveResponse(storeId, "PENDING");

		Mockito.when(storeService.createStore(Mockito.any(StoreApproveRequest.class))).thenReturn(response);

		mockMvc.perform(post("/store")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeId").value(storeId.toString()))
			.andExpect(jsonPath("$.storeApprovalStatus").value("PENDING"));

	}
}
