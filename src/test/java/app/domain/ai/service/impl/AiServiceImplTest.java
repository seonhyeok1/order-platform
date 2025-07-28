package app.domain.ai.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import app.domain.ai.model.AiHistoryRepository;
import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.model.entity.AiHistory;
import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.model.entity.enums.ReqType;
import app.domain.ai.service.AiService;

@SpringBootTest
class AiServiceImplTest {

	@Autowired
	private AiService aiService;

	@Autowired
	private AiHistoryRepository aiHistoryRepository;

	@Test
	void generateDescription() {
		// Given
		AiRequest aiRequest = new AiRequest();
		aiRequest.setStoreName("미스터피자");
		aiRequest.setMenuName("고구마 피자");
		aiRequest.setReqType(ReqType.MENU_DESCRIPTION);
		aiRequest.setPromptText("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		// When
		AiResponse aiResponse = aiService.generateDescription(aiRequest);

		// Then
		assertNotNull(aiResponse.getRequest_id());
		assertFalse(aiResponse.getGenerated_content().isEmpty()); // 내용이 비어있지 않음을 확인
	}

	@Test
	@Transactional
	void saveAiRequestToDatabase() {
		// Given
		AiRequest aiRequest = new AiRequest();
		aiRequest.setStoreName("미스터피자");
		aiRequest.setMenuName("고구마 피자");
		aiRequest.setReqType(ReqType.MENU_DESCRIPTION);
		aiRequest.setPromptText("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

		// When
		AiResponse aiResponse = aiService.generateDescription(aiRequest);

		// Then
		assertNotNull(aiResponse.getRequest_id());
		assertFalse(aiResponse.getGenerated_content().isEmpty()); // 내용이 비어있지 않음을 확인

		// 데이터베이스에서 저장된 AiHistory 조회 및 검증
		AiHistory savedAiHistory = aiHistoryRepository.findById(java.util.UUID.fromString(aiResponse.getRequest_id()))
			.orElse(null);

		assertNotNull(savedAiHistory);
		assertEquals("미스터피자", savedAiHistory.getStoreName());
		assertEquals("고구마 피자", savedAiHistory.getMenuName());
		assertEquals(ReqType.MENU_DESCRIPTION, savedAiHistory.getReqType());
		assertEquals("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘", savedAiHistory.getPromptText());
		assertFalse(savedAiHistory.getGeneratedContent().isEmpty()); // 내용이 비어있지 않음을 확인
		assertEquals(AiRequestStatus.SUCCESS, savedAiHistory.getStatus());
	}
}