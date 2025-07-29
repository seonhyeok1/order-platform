package app.domain.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI 글쓰기 도우미 관련 API")
@RequestMapping("/api/ai")
public class AiController {

	private final AiService aiService;

	@PostMapping("/generate")
	@Operation(
		summary = "AI 글쓰기 도우미",
		description = "가게 또는 메뉴 설명을 AI를 통해 생성합니다.",
		// Swagger UI에 표시될 요청 본문 예시를 정의합니다
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "가게 설명 생성 요청 예시",
						summary = "가게 설명 생성",
						value = "{" +
							"\"storeName\": \"카페 아모르\", " +
							"\"menuName\": \"\", " + // 가게 설명이므로 menuName은 빈 스트링
							"\"reqType\": \"STORE_DESCRIPTION\", " + // ReqType enum 값 중 하나를 사용
							"\"promptText\": \"아늑하고 따뜻한 분위기의 감성적인 카페\"" +
							"}"
					),
					@ExampleObject(
						name = "메뉴 설명 생성 요청 예시",
						summary = "메뉴 설명 생성",
						value = "{" +
							"\"storeName\": \"베이커리 블리스\", " +
							"\"menuName\": \"크로와상\", " +
							"\"reqType\": \"MENU_DESCRIPTION\", " + // ReqType enum 값 중 하나를 사용
							"\"promptText\": \"겉은 바삭하고 속은 촉촉한 프랑스 전통 크로와상\"" +
							"}"
					)
				}
			)
		)
	)
	public ApiResponse<AiResponse> generateDescription(@RequestBody @Valid AiRequest aiRequest) {
		AiResponse response = aiService.generateDescription(aiRequest);
		return ApiResponse.onSuccess(response);
	}
}
