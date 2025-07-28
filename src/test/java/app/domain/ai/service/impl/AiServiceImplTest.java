package app.domain.ai.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import app.domain.ai.model.AiHistoryRepository;
import app.domain.ai.model.dto.request.AiRequest;
import app.domain.ai.model.dto.response.AiResponse;
import app.domain.ai.model.entity.AiHistory;
import app.domain.ai.model.entity.enums.AiRequestStatus;
import app.domain.ai.model.entity.enums.ReqType;
import app.domain.ai.service.AiService;

@DisplayName("AiService 테스트")
class AiServiceImplTest {

	@Nested
	@DisplayName("단위 테스트 (Mockito)")
	@ExtendWith(MockitoExtension.class)
	class AiServiceUnitTest {

		@InjectMocks
		private AiServiceImpl aiService;

		@Mock
		private AiHistoryRepository aiHistoryRepository;

		// ChatClient의 각 체인 단계를 Mocking하기 위한 Mock 객체들
		@Mock
		private ChatClient chatClient;
		@Mock
		private ChatClient.ChatClientRequestSpec chatClientRequestSpec; // prompt()의 반환 타입
		@Mock
		private ChatClient.CallResponseSpec callResponseSpec; // call()의 최종 반환 타입 (content()를 가짐)

		@Captor
		private ArgumentCaptor<AiHistory> aiHistoryCaptor;

		private AiRequest aiRequest;
		private AiHistory savedHistory; // Service 로직 내에서 상태가 변경될 객체

		@BeforeEach
		void setUp() {
			aiRequest = new AiRequest();
			aiRequest.setStoreName("맛있는 족발집");
			aiRequest.setMenuName("반반 족발");
			aiRequest.setReqType(ReqType.MENU_DESCRIPTION);
			aiRequest.setPromptText("쫄깃하고 부드러운 식감을 강조해주세요.");

			savedHistory = AiHistory.builder()
				.aiRequestId(UUID.randomUUID())
				.storeName(aiRequest.getStoreName())
				.menuName(aiRequest.getMenuName())
				.reqType(aiRequest.getReqType())
				.promptText(aiRequest.getPromptText())
				.status(AiRequestStatus.PENDING)
				.build();

			when(aiHistoryRepository.save(any(AiHistory.class))).thenReturn(savedHistory);

			// --- ChatClient의 메서드 체인을 Mocking ---
			// 1. chatClient.prompt() 호출 시 chatClientRequestSpec 반환
			when(chatClient.prompt()).thenReturn(chatClientRequestSpec);

			// 2. chatClientRequestSpec.user(anyString()) 호출 시 chatClientRequestSpec 자기 자신 반환
			when(chatClientRequestSpec.user(anyString())).thenReturn(chatClientRequestSpec);

			// 3. chatClientRequestSpec.options(any(ChatOptions.class)) 호출 시 chatClientRequestSpec 자기 자신 반환
			//    (서비스 로직에서 ChatOptions를 사용한다면 필요)
			when(chatClientRequestSpec.options(any(ChatOptions.class))).thenReturn(chatClientRequestSpec);

			// 4. chatClientRequestSpec.call() 호출 시 callResponseSpec 반환
			when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
			// --- Mocking 체인 끝 ---
		}

		@Test
		@DisplayName("시나리오 1: 요청 시 DB에 PENDING 상태로 저장되는지 확인")
		void savePendingStatusTest() {
			// given
			when(callResponseSpec.content()).thenReturn("AI 응답"); // 변경: responseSpec -> callResponseSpec

			// when
			aiService.generateDescription(aiRequest);

			// then
			verify(aiHistoryRepository, times(1)).save(aiHistoryCaptor.capture());
			AiHistory capturedOnSave = aiHistoryCaptor.getValue();

			assertEquals(AiRequestStatus.PENDING, capturedOnSave.getStatus());
			assertEquals("맛있는 족발집", capturedOnSave.getStoreName());
		}

		@Test
		@DisplayName("시나리오 2: Input/Output이 올바른지 성공 케이스 확인")
		void generateDescription_Success_IO_Test() {
			// given
			String expectedContent = "쫄깃함과 부드러움이 공존하는 환상의 맛! 저희 가게 대표 메뉴 반반 족발입니다.";
			when(callResponseSpec.content()).thenReturn(expectedContent); // 변경: responseSpec -> callResponseSpec

			// when
			AiResponse response = aiService.generateDescription(aiRequest);

			// then
			assertNotNull(response);
			assertEquals(expectedContent, response.getGeneratedContent());

			assertEquals(AiRequestStatus.SUCCESS, savedHistory.getStatus());
			assertEquals(expectedContent, savedHistory.getGeneratedContent());
		}

		@Test
		@DisplayName("시나리오 2: Input/Output이 올바른지 실패 케이스 확인")
		void generateDescription_Failure_IO_Test() {
			// given
			when(callResponseSpec.content()).thenThrow(
				new RuntimeException("AI 모델 호출 실패"));

			// when & then
			assertThrows(RuntimeException.class, () -> aiService.generateDescription(aiRequest));

			// verify
			assertEquals(AiRequestStatus.FAILED, savedHistory.getStatus());
			assertTrue(savedHistory.getGeneratedContent().contains("Error: AI 모델 호출 실패"));
		}
	}

	@Nested
	@DisplayName("시나리오 3: 연동 테스트 (@SpringBootTest)")
	@SpringBootTest
	@ActiveProfiles("test") // test용 application.yml(properties) 사용
	class AiServiceIntegrationTest {

		@Autowired
		private AiService aiService;

		@Autowired
		private AiHistoryRepository aiHistoryRepository;

		@Test
		@DisplayName("실제 AI API에 접속하여 응답을 받아오고 DB에 SUCCESS로 저장되는지 확인")
		@Transactional
		void generateDescription_IntegrationTest() {
			// given
			AiRequest realRequest = new AiRequest();
			realRequest.setStoreName("미스터피자");
			realRequest.setMenuName("고구마 피자");
			realRequest.setReqType(ReqType.MENU_DESCRIPTION);
			realRequest.setPromptText("달콤하고 부드러운 점을 강조해서 30자 이내로 간략한 상품 설명을 작성해줘");

			// when
			AiResponse aiResponse = aiService.generateDescription(realRequest);

			// then
			assertNotNull(aiResponse.getRequestId(), "응답으로 받은 ID가 null이 아니어야 합니다.");
			assertNotNull(aiResponse.getGeneratedContent(), "생성된 내용이 null이 아니어야 합니다.");
			assertFalse(aiResponse.getGeneratedContent().isEmpty(), "생성된 내용이 비어있지 않아야 합니다.");
			System.out.println("AI 생성 결과: " + aiResponse.getGeneratedContent());

			// DB에서 직접 조회하여 검증
			AiHistory savedHistory = aiHistoryRepository.findById(UUID.fromString(aiResponse.getRequestId()))
				.orElse(null);

			assertNotNull(savedHistory, "DB에 해당 ID의 기록이 저장되어 있어야 합니다.");
			assertEquals(AiRequestStatus.SUCCESS, savedHistory.getStatus(), "최종 상태는 SUCCESS여야 합니다.");
			assertEquals(aiResponse.getGeneratedContent(), savedHistory.getGeneratedContent(),
				"응답 내용과 DB에 저장된 내용이 같아야 합니다.");
		}

	}
}