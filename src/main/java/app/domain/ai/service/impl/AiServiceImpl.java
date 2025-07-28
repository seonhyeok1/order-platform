package app.domain.ai.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import app.domain.ai.model.dto.request.AiRequestDto;
import app.domain.ai.model.dto.response.AiResponseDto;
import app.domain.ai.service.AiService;

@Service
public class AiServiceImpl implements AiService {

	@Override
	public AiResponseDto generateDescription(AiRequestDto aiRequestDto) {
		String requestId = UUID.randomUUID().toString();

		// TODO: AI 모델에 요청을 보내고 응답을 받는 로직 구현
		String generatedContent = "AI 모델로부터 생성된 컨텐츠입니다.";

		return AiResponseDto.builder()
			.request_id(requestId)
			.generated_content(generatedContent)
			.build();
	}
}
