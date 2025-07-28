package app.domain.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.ai.model.dto.request.AiRequestDto;
import app.domain.ai.model.dto.response.AiResponseDto;
import app.domain.ai.service.AiService;
import app.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

	private final AiService aiService;

	@PostMapping("/generate")
	public ApiResponse<AiResponseDto> generateDescription(@RequestBody AiRequestDto aiRequestDto) {
		AiResponseDto responseDto = aiService.generateDescription(aiRequestDto);
		return ApiResponse.onSuccess(responseDto);
	}
}
