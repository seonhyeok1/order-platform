package app.domain.ai.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.ai.model.AiHistoryRepository;
import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.model.entity.AiHistory;
import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AiServiceImpl implements AiService {

	private final AiHistoryRepository aiHistoryRepository;

	@Override
	public AiResponse generateDescription(AiRequest aiRequest) {
		AiHistory aiRequestEntity = AiHistory.builder()
			.storeName(aiRequest.getStoreName())
			.menuName(aiRequest.getMenuName())
			.reqType(aiRequest.getReqType())
			.promptText(aiRequest.getPromptText())
			.status(AiRequestStatus.PENDING)
			.build();

		AiHistory savedAiRequestEntity = aiHistoryRepository.save(aiRequestEntity);

		// TODO: AI 모델에 요청을 보내고 응답을 받는 로직 구현
		String generatedContent = "AI 모델로부터 생성된 컨텐츠입니다.";

		savedAiRequestEntity.updateGeneratedContent(generatedContent, AiRequestStatus.SUCCESS);

		return AiResponse.builder()
			.request_id(savedAiRequestEntity.getAiRequestId().toString())
			.generated_content(generatedContent)
			.build();
	}
}