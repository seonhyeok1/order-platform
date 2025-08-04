package app.unit.domain.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.store.RegionController;
import app.domain.store.RegionService;
import app.domain.store.status.StoreErrorCode;
import app.domain.store.status.StoreSuccessStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class RegionControllerTest {

	@InjectMocks
	RegionController regionController;

	@Mock
	RegionService regionService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(regionController)
			.build();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("성공: 유효한 지역 코드")
	void getRegionIdByCodeSuccess() throws Exception {
		String regionCode = "1111010600"; //광화문 지역 코드
		UUID regionId = UUID.randomUUID();

		when(regionService.getRegionIdByCode(regionCode)).thenReturn(regionId);

		mockMvc.perform(post("/region/code/{regionId}", regionCode))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value(StoreSuccessStatus._OK.getCode()))
			.andExpect(jsonPath("$.message").value(StoreSuccessStatus._OK.getMessage()))
			.andExpect(jsonPath("$.result").value(regionId.toString()));

		verify(regionService, times(1)).getRegionIdByCode(regionCode);
	}

	@Test
	@DisplayName("실패: RegionCode 누락 (null)")
	void getRegionIdByCodeRegionCodeNull() {
		GeneralException exception = assertThrows(GeneralException.class, () -> {
			regionController.getRegionIdByCode(null);
		});
		assertEquals(StoreErrorCode.REGIONCODE_NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("실패: RegionCode 누락 (blank)")
	void getRegionIdByCodeRegionCodeBlank() {
		GeneralException exception = assertThrows(GeneralException.class, () -> {
			regionController.getRegionIdByCode("");
		});
		assertEquals(StoreErrorCode.REGIONCODE_NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("실패: RegionId 존재하지 않음")
	void getRegionIdByCodeRegionIdNotFound() {
		String regionCode = "1111010600";

		when(regionService.getRegionIdByCode(regionCode)).thenThrow(
			new GeneralException(StoreErrorCode.REGION_NOT_FOUND));

		GeneralException exception = assertThrows(GeneralException.class, () -> {
			regionController.getRegionIdByCode(regionCode);
		});
		assertEquals(StoreErrorCode.REGION_NOT_FOUND, exception.getCode());
	}
}