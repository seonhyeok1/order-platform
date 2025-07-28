package app.domain.ai.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.ai.model.AiRequestRepository;
import app.domain.ai.model.dto.request.AiRequestDto;
import app.domain.ai.model.dto.response.AiResponseDto;
import app.domain.ai.model.entity.AiRequest;
import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AiServiceImpl implements AiService {

	private final AiRequestRepository aiRequestRepository;

	@Override
	public AiResponseDto generateDescription(AiRequestDto aiRequestDto) {
		AiRequest aiRequest = AiRequest.builder()
			.storeName(aiRequestDto.getStoreName())
			.menuName(aiRequestDto.getMenuName())
			.reqType(aiRequestDto.getReqType())
			.promptText(aiRequestDto.getPromptText())
			.status(AiRequestStatus.PENDING)
			.build();

		AiRequest savedAiRequest = aiRequestRepository.save(aiRequest);

		// TODO: AI 모델에 요청을 보내고 응답을 받는 로직 구현
		String generatedContent = "AI 모델로부터 생성된 컨텐츠입니다.";

		savedAiRequest.updateGeneratedContent(generatedContent, AiRequestStatus.SUCCESS);

		return AiResponseDto.builder()
			.request_id(savedAiRequest.getAiRequestId().toString())
			.generated_content(generatedContent)
			.build();
	}
}