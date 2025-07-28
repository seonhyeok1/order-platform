package app.domain.ai.service.impl;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
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
			PromptTemplate promptTemplate = new PromptTemplate("""
				너는 사용자의 요청에 맞춰 배달앱에 적합한 마케팅 문구를 생성하는 AI야. 아래 주어진 정보를 바탕으로 멋진 결과물을 만들어줘.
				
				- 가게 이름: {storeName}
				- 메뉴 이름: {menuName}
				- 요청 종류: {reqType}
				- 핵심 요청사항 : {promptText}
				
				요청 종류가 MENU_DESCRIPTION 이면 30자 이내로 작성해주고 STORE_DESCRIPTION 이면 100자 이내로 작성해줘.
				""");
			Prompt prompt = promptTemplate.create(Map.of(
				"storeName", aiRequest.getStoreName(), "menuName", aiRequest.getMenuName()
				, "reqType", aiRequest.getReqType(), "promptText", aiRequest.getPromptText())
			);

			generatedContent = chatClient.prompt()
				.options(OpenAiChatOptions.builder().model("gpt-4.1-mini").build())
				.user(prompt.getContents())
				.call()
				.content();
			savedAiRequestEntity.updateGeneratedContent(generatedContent, AiRequestStatus.SUCCESS);
		} catch (Exception e) {
			// Handle exceptions during AI model call
			savedAiRequestEntity.updateGeneratedContent("Error: " + e.getMessage(), AiRequestStatus.FAILED);
			throw new RuntimeException("Failed to generate AI content", e); // Re-throw or handle as appropriate
		}

		return AiResponse.builder()
			.requestId(savedAiRequestEntity.getAiRequestId().toString())
			.generatedContent(generatedContent)
			.build();
	}
}