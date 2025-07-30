package app.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.domain.auth.AuthService;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.dto.CreateUserReq;
import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService.createUser 테스트")
class AuthServiceCreateUserTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@InjectMocks
	private AuthService authService;

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

	private void givenNoDuplicates(CreateUserReq req) {
		given(userRepository.existsByUsername(req.getUsername())).willReturn(false);
		given(userRepository.existsByEmail(req.getEmail())).willReturn(false);
		given(userRepository.existsByNickname(req.getNickname())).willReturn(false);
		given(userRepository.existsByPhoneNumber(req.getPhoneNumber())).willReturn(false);
	}

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {

		@Test
		@DisplayName("고객 계정 생성 성공")
		void createUser_ValidInput_Success() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			User user = User.builder()
				.userId(1L)
				.username(req.getUsername())
				.password("encodedPassword")
				.userRole(UserRole.CUSTOMER)
				.build();

			// 중복 검사 통과
			givenNoDuplicates(req);
			given(passwordEncoder.encode(req.getPassword())).willReturn("encodedPassword");
			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			String resultUserId = authService.createUser(req);

			// then
			// 1. ArgumentCaptor를 사용하여 save 메서드에 전달된 User 객체를 캡처합니다.
			ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userArgumentCaptor.capture());
			User capturedUser = userArgumentCaptor.getValue();

			// 2. 캡처된 객체의 필드들을 명시적으로 검증합니다.
			// 결과가 출력되도록 각 필드를 개별적으로 검증하여 어떤 값이 잘못되었는지 명확히 알 수 있습니다.
			assertThat(capturedUser.getUsername()).isEqualTo(req.getUsername());
			assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
			assertThat(capturedUser.getEmail()).isEqualTo(req.getEmail());
			assertThat(capturedUser.getNickname()).isEqualTo(req.getNickname());
			// **요청한 Role(CUSTOMER)이 정확히 들어갔는지 확인**
			assertThat(capturedUser.getUserRole()).isEqualTo(UserRole.CUSTOMER);

			// 3. 반환된 userId가 올바른지 확인합니다.
			assertThat(resultUserId).isEqualTo("1");
		}

		@Test
		@DisplayName("점주 계정 생성 성공")
		void createOwner_Success() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.OWNER);
			User user = User.builder()
				.userId(2L)
				.username(req.getUsername())
				.password("encodedPassword")
				.userRole(UserRole.OWNER)
				.build();

			givenNoDuplicates(req);
			given(passwordEncoder.encode(req.getPassword())).willReturn("encodedPassword");
			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			String resultUserId = authService.createUser(req);

			// then
			ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userArgumentCaptor.capture());
			User capturedUser = userArgumentCaptor.getValue();

			// **요청한 Role(OWNER)이 정확히 들어갔는지 확인**
			assertThat(capturedUser.getUserRole()).isEqualTo(UserRole.OWNER);
			assertThat(resultUserId).isEqualTo("2");
		}
	}

	@Nested
	@DisplayName("중복 검증 실패")
	class ValidationFailure {

		@Test
		@DisplayName("중복된 아이디로 가입 시 예외 발생")
		void duplicateUsername_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			// existsByUsername이 true를 반환하도록 설정
			given(userRepository.existsByUsername(req.getUsername())).willReturn(true);

			// when & then
			// 1. 예외가 발생하는지 검증
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.USER_ALREADY_EXISTS.getCode());

			// 2. 중복 확인 후 save 메서드가 절대 호출되지 않았는지 확인
			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 이메일로 가입 시 예외 발생")
		void duplicateEmail_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.existsByUsername(req.getUsername())).willReturn(false);
			// existsByEmail이 true를 반환하도록 설정
			given(userRepository.existsByEmail(req.getEmail())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.EMAIL_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 닉네임으로 가입 시 예외 발생")
		void duplicateNickname_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.existsByUsername(req.getUsername())).willReturn(false);
			given(userRepository.existsByEmail(req.getEmail())).willReturn(false);
			// existsByNickname이 true를 반환하도록 설정
			given(userRepository.existsByNickname(req.getNickname())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.NICKNAME_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복된 휴대폰 번호로 가입 시 예외 발생")
		void duplicatePhoneNumber_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.existsByUsername(req.getUsername())).willReturn(false);
			given(userRepository.existsByEmail(req.getEmail())).willReturn(false);
			given(userRepository.existsByNickname(req.getNickname())).willReturn(false);
			// existsByPhoneNumber가 true를 반환하도록 설정
			given(userRepository.existsByPhoneNumber(req.getPhoneNumber())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS.getCode());

			verify(userRepository, never()).save(any(User.class));
		}
	}

	@Nested
	@DisplayName("시스템 예외 상황")
	class SystemException {

		@Test
		@DisplayName("데이터베이스 저장 실패 시 예외 발생")
		void databaseError_OnSave_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			// 모든 중복 검사를 통과하도록 설정
			givenNoDuplicates(req);
			// save 메서드에서 예외 발생
			given(userRepository.save(any(User.class))).willThrow(new DataAccessException("DB connection failed") {
			});

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.extracting("errorReasonHttpStatus.code")
				.isEqualTo(ErrorStatus._INTERNAL_SERVER_ERROR.getCode());
		}

		@Test
		@DisplayName("비밀번호 인코딩 실패 시 예외 발생")
		void passwordEncoding_Fail_ThrowsException() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			// 중복 검사는 통과하도록 설정
			givenNoDuplicates(req);
			// passwordEncoder에서 예외가 발생하도록 설정
			given(passwordEncoder.encode(req.getPassword())).willThrow(new RuntimeException("Encoding failed"));

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Encoding failed");

			// 예외 발생 후 save가 호출되지 않았는지 확인
			verify(userRepository, never()).save(any(User.class));
		}
	}
}