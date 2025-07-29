package app.domain.ai;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.model.entity.enums.ReqType;
import app.global.apiPayload.exception.ExceptionAdvice;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiController 테스트")
class AiControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private AiService aiService;

	@InjectMocks
	private AiController aiController;

	@BeforeEach
	void setUp() {
		mockMvc =
			MockMvcBuilders.standaloneSetup(aiController)
				.setControllerAdvice(
					new ExceptionAdvice())
				.addFilter(new CharacterEncodingFilter("UTF-8", true))
				.build();
	}

	@Test
	@DisplayName("AI 생성 요청 성공")
	void givenValidRequest_whenGenerateDescription_thenReturnsSuccess() throws Exception {
		// Given
		AiRequest request =
			new AiRequest("맛있는 족발집", "반반 족발", ReqType.MENU_DESCRIPTION, "쫄깃하고 부드러운 식감을 강조해주세요.");
		AiResponse response = new AiResponse(UUID.randomUUID().toString(), "AI 응답");
		given(aiService.generateDescription(any(AiRequest.class))).willReturn(response);

		// When & Then
		mockMvc
			.perform(
				post("/api/ai/generate")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result.requestId").value(response.getRequestId()))
			.andExpect(jsonPath("$.result.generatedContent").value(response.getGeneratedContent()));
	}

	@Test
	@DisplayName("AI 생성 요청 시 입력값이 유효하지 않으면 실패")
	void givenInvalidRequest_whenGenerateDescription_thenReturnsFailure() throws Exception {
		AiRequest invalidRequest =
			new AiRequest(null, "반반 족발", ReqType.MENU_DESCRIPTION, "쫄깃하고 부드러운 식감을 강조해주세요.");

		// When & Then
		mockMvc
			.perform(
				post("/api/ai/generate")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidRequest)))
			.andExpect(status().isBadRequest());
	}
}
