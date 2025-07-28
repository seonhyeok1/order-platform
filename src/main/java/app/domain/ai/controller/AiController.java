package app.domain.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.service.AiService;
import app.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

	private final AiService aiService;

	@PostMapping("/generate")
	public ApiResponse<AiResponse> generateDescription(@RequestBody @Valid AiRequest aiRequest) {
		AiResponse response = aiService.generateDescription(aiRequest);
		return ApiResponse.onSuccess(response);
	}
}
