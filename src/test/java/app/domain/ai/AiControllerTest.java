package app.domain.ai;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.model.entity.enums.ReqType;

@WebMvcTest(AiController.class)
class AiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper; // To convert objects to JSON

	@MockitoBean
	private AiService aiService;

	@Test
	void generateDescription_Success_MENU_DESCRIPTION() throws Exception {
		AiRequest request = new AiRequest("미스터피자", "고구마 피자", ReqType.MENU_DESCRIPTION,
			"고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		AiResponse mockResponse = new AiResponse("test-id-123", "생성된 고구마 피자 설명입니다.");

		when(aiService.generateDescription(any(AiRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.result.request_id").value("test-id-123"))
			.andExpect(jsonPath("$.result.generated_content").value("생성된 고구마 피자 설명입니다."));
	}

	@Test
	void generateDescription_Success_STORE_DESCRIPTION() throws Exception {
		AiRequest request = new AiRequest("미스터피자", null, ReqType.STORE_DESCRIPTION, "우리 가게는 30년 전통의 피자 맛집이야");

		AiResponse mockResponse = new AiResponse("test-id-456", "생성된 미스터피자 가게 설명입니다.");

		when(aiService.generateDescription(any(AiRequest.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.result.request_id").value("test-id-456"))
			.andExpect(jsonPath("$.result.generated_content").value("생성된 미스터피자 가게 설명입니다."));
	}

	@Test
	void generateDescription_ValidationFail_MissingStoreName() throws Exception {
		AiRequest request = new AiRequest(null, "고구마 피자", ReqType.MENU_DESCRIPTION,
			"고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("COMMON400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.result.storeName").exists()); // Check that storeName error exists
	}

	@Test
	void generateDescription_ValidationFail_MissingPromptText() throws Exception {
		AiRequest request = new AiRequest("미스터피자", "고구마 피자", ReqType.MENU_DESCRIPTION, null);

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("COMMON400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(jsonPath("$.result.promptText").exists()); // Check that promptText error exists
	}

	@Test
	void generateDescription_ValidationFail_MENU_DESCRIPTION_MissingMenuName() throws Exception {
		AiRequest request = new AiRequest("미스터피자", null, ReqType.MENU_DESCRIPTION,
			"고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("COMMON400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(
				jsonPath("$.result.menuName").value("MENU_DESCRIPTION 요청은 메뉴 이름이 필수입니다.")); // Custom error message
	}

	@Test
	void generateDescription_ValidationFail_MENU_DESCRIPTION_BlankMenuName() throws Exception {
		AiRequest request = new AiRequest("미스터피자", "", ReqType.MENU_DESCRIPTION,
			"고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		mockMvc.perform(post("/api/ai/generate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("COMMON400"))
			.andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
			.andExpect(
				jsonPath("$.result.menuName").value("MENU_DESCRIPTION 요청은 메뉴 이름이 필수입니다.")); // Custom error message
	}
}
