package app.domain.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
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
	private final ChatClient chatClient;

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

		String generatedContent;
		try {
			// Spring AI ChatClient를 사용하여 AI 모델에 요청 보내기
			generatedContent = chatClient.prompt()
				.user(aiRequest.getPromptText())
				.call()
				.content();
			savedAiRequestEntity.updateGeneratedContent(generatedContent, AiRequestStatus.SUCCESS);
		} catch (Exception e) {
			// Handle exceptions during AI model call
			savedAiRequestEntity.updateGeneratedContent("Error: " + e.getMessage(), AiRequestStatus.FAILED);
			throw new RuntimeException("Failed to generate AI content", e); // Re-throw or handle as appropriate
		}

		return AiResponse.builder()
			.request_id(savedAiRequestEntity.getAiRequestId().toString())
			.generated_content(generatedContent)
			.build();
	}
}