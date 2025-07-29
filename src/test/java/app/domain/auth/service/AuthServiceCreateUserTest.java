package app.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
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
		@DisplayName("점주 계정 생성 성공. ROLE : OWNER")
		void createAdmin_Success() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.OWNER);
			User user = User.builder()
				.userId(2L)
				.username(req.getUsername())
				.password("encodedPassword")
				.userRole(UserRole.OWNER)
				.build();

			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
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
	@DisplayName("입력 검증 실패")
	class ValidationFailure {

		@Test
		@DisplayName("중복 username(아이디)")
		void duplicateUsername() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.of(User.builder().build()));

			// when & then
			// 1. 예외가 발생하는지 검증
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.USER_ALREADY_EXISTS);

			// 2. **중복 확인 후 save 메서드가 절대 호출되지 않았는지 확인**
			verify(userRepository, never()).save(any(User.class));
		}

		@Test
		@DisplayName("중복 이메일")
		void duplicateEmail() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"could not execute statement",
				new ConstraintViolationException(
					"ERROR: duplicate key value violates unique constraint \"p_user_email_key\"", null,
					"p_user_email_key")
			);

			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
			given(userRepository.save(any(User.class))).willThrow(exception);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.EMAIL_ALREADY_EXISTS);
		}

		@Test
		@DisplayName("중복 닉네임")
		void duplicateNickname() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"could not execute statement",
				new ConstraintViolationException(
					"ERROR: duplicate key value violates unique constraint \"p_user_nickname_key\"", null,
					"p_user_nickname_key")
			);

			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
			given(userRepository.save(any(User.class))).willThrow(exception);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.NICKNAME_ALREADY_EXISTS);
		}

		@Test
		@DisplayName("중복 휴대폰 번호")
		void duplicatePhonenumber() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"could not execute statement",
				new ConstraintViolationException(
					"ERROR: duplicate key value violates unique constraint \"p_user_phone_number_key\"", null,
					"p_user_phone_number_key")
			);

			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
			given(userRepository.save(any(User.class))).willThrow(exception);

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorStatus", ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
		}
	}

	@Nested
	@DisplayName("예외 상황")
	class SystemException {

		@Test
		@DisplayName("데이터베이스 에러 - 데이터베이스 저장 실패")
		void databaseError() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
			given(userRepository.save(any(User.class))).willThrow(new DataAccessException("DB connection failed") {
			});

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorStatus", ErrorStatus._INTERNAL_SERVER_ERROR);
		}

		@Test
		@DisplayName("비밀번호 인코딩 실패")
		void externalServiceError() {
			// given
			CreateUserReq req = createValidUserReq(UserRole.CUSTOMER);
			given(userRepository.findByUsername(req.getUsername())).willReturn(Optional.empty());
			given(passwordEncoder.encode(req.getPassword())).willThrow(new RuntimeException("Encoding failed"));

			// when & then
			assertThatThrownBy(() -> authService.createUser(req))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Encoding failed");
		}
	}
}