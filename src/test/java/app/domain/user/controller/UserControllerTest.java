package app.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.user.UserController;
import app.domain.user.UserService;
import app.domain.user.model.dto.CreateUserReq;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.code.status.SuccessStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.SecurityConfig;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, UserControllerTest.TestConfig.class})    // securityConfig, TestConfig 명시적 선언
@DisplayName("AuthController 테스트")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	@TestConfiguration
	static class TestConfig {
		@Bean
		public UserService authService() {
			return mock(UserService.class);
		}
	}

	// 테스트용 요청 DTO를 생성하는 헬퍼 메서드
	private CreateUserReq createValidUserReq(UserRole role) {
		CreateUserReq req = new CreateUserReq();
		req.setUsername("testuser");
		req.setPassword("password123!");
		req.setEmail("test@example.com");
		req.setNickname("testnick");
		req.setRealName("김테스트");
		req.setPhoneNumber("01012345678");
		req.setUserRole(role);
		return req;
	}

	@Nested
	@DisplayName("회원가입 API [/api/auth/signup] 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("성공: 유효한 정보로 회원가입을 요청하면 200 OK와 생성된 사용자 ID를 반환한다.")
		void createUser_Success() throws Exception {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			String expectedUserId = "1";
			// authService.createUser가 호출되면 "1"을 반환하도록 설정
			given(userService.createUser(any(CreateUserReq.class))).willReturn(expectedUserId);

			// when
			// /api/auth/signup 경로로 POST 요청을 보냄
			ResultActions resultActions = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			// 응답 상태가 200 OK이고, 응답 본문의 각 필드가 예상과 일치하는지 검증
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value(SuccessStatus._OK.getCode()))
				.andExpect(jsonPath("$.message").value(SuccessStatus._OK.getMessage()))
				.andExpect(jsonPath("$.result").value(expectedUserId))
				.andDo(print()); // 요청/응답 전체 내용 출력
		}

		@Test
		@DisplayName("실패(유효성 검증): 아이디가 누락된 요청은 400 Bad Request를 반환한다.")
		void createUser_Fail_Validation() throws Exception {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			req.setUsername(" "); // 아이디를 빈 값으로 설정하여 유효성 검증(@NotBlank) 실패 유도

			// when
			ResultActions resultActions = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			// 응답 상태가 400 Bad Request이고, 응답 본문에 유효성 검증 실패 정보가 포함되는지 검증
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.resultCode").value(ErrorStatus._BAD_REQUEST.getCode()))
				.andExpect(jsonPath("$.message").value(ErrorStatus._BAD_REQUEST.getMessage()))
				.andExpect(jsonPath("$.result.username").exists())
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 이미 존재하는 아이디로 회원가입을 요청하면 409 Conflict를 반환한다.")
		void createUser_Fail_DuplicateUsername() throws Exception {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			// authService.createUser가 호출되면 USER_ALREADY_EXISTS 예외를 던지도록
			given(userService.createUser(any(CreateUserReq.class)))
				.willThrow(new GeneralException(ErrorStatus.USER_ALREADY_EXISTS));

			// when
			ResultActions resultActions = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)));

			// then
			// 응답 상태가 409 Conflict이고, 응답 본문이 USER_ALREADY_EXISTS 에러 정보와 일치하는지 검증
			resultActions
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.resultCode").value(ErrorStatus.USER_ALREADY_EXISTS.getCode()))
				.andExpect(jsonPath("$.message").value(ErrorStatus.USER_ALREADY_EXISTS.getMessage()))
				.andExpect(jsonPath("$.result").doesNotExist())
				.andDo(print());
		}
	}
}