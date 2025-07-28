package app.domain.ai.service.impl;

import app.domain.ai.model.AiRequestRepository;
import app.domain.ai.model.dto.request.AiRequestDto;
import app.domain.ai.model.dto.response.AiResponseDto;
import app.domain.ai.model.entity.AiRequest;
import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.model.entity.enums.ReqType;
import app.domain.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AiServiceImplTest {

    @Autowired
    private AiService aiService;

    @Autowired
    private AiRequestRepository aiRequestRepository;

    @Test
    void generateDescription() {
        // Given
        AiRequestDto aiRequestDto = new AiRequestDto();
        // AiRequestDto 필드 설정 (예시)
        aiRequestDto.setStoreName("미스터피자");
        aiRequestDto.setMenuName("고구마 피자");
        aiRequestDto.setReqType(ReqType.MENU_DESCRIPTION);
        aiRequestDto.setPromptText("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

        // When
        AiResponseDto aiResponseDto = aiService.generateDescription(aiRequestDto);

        // Then
        assertNotNull(aiResponseDto.getRequest_id());
        assertNotNull(aiResponseDto.getGenerated_content());
    }

    @Test
    @Transactional
    void saveAiRequestToDatabase() {
        // Given
        AiRequestDto aiRequestDto = new AiRequestDto();
        aiRequestDto.setStoreName("미스터피자");
        aiRequestDto.setMenuName("고구마 피자");
        aiRequestDto.setReqType(ReqType.MENU_DESCRIPTION);
        aiRequestDto.setPromptText("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘");

        // When
        AiResponseDto aiResponseDto = aiService.generateDescription(aiRequestDto);

        // Then
        assertNotNull(aiResponseDto.getRequest_id());
        assertNotNull(aiResponseDto.getGenerated_content());

        // 데이터베이스에서 저장된 AiRequest 조회 및 검증
        AiRequest savedAiRequest = aiRequestRepository.findById(java.util.UUID.fromString(aiResponseDto.getRequest_id()))
                .orElse(null);

        assertNotNull(savedAiRequest);
        assertEquals("미스터피자", savedAiRequest.getStoreName());
        assertEquals("고구마 피자", savedAiRequest.getMenuName());
        assertEquals(ReqType.MENU_DESCRIPTION, savedAiRequest.getReqType());
        assertEquals("고구마 피자에 대해 달콤하고 부드러운 점을 강조해서 간략한 상품 설명을 작성해줘", savedAiRequest.getPromptText());
        assertEquals("AI 모델로부터 생성된 컨텐츠입니다.", savedAiRequest.getGeneratedContent());
        assertEquals(AiRequestStatus.SUCCESS, savedAiRequest.getStatus());
    }
}