package app.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.anyLong;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.domain.auth.AuthService;
import app.domain.auth.model.dto.request.LoginRequest;
import app.domain.auth.model.dto.response.LoginResponse;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.jwt.JwtTokenProvider;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능을 사용해 Mock 객체를 초기화합니다.
@DisplayName("AuthService 로그인 테스트")
class AuthServiceLoginTest {

	@InjectMocks
	private AuthService authService; // 테스트 대상 클래스. @Mock으로 선언된 객체들이 주입됩니다.

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations; // RedisTemplate의 opsForValue()가 반환하는 객체를 모킹

	@BeforeEach
	void setUp() {
		// redisTemplate.opsForValue()가 호출될 때, 모킹된 valueOperations를 반환하도록 설정
		// 이 설정이 없으면 redisTemplate.opsForValue()가 null을 반환하여 NullPointerException 발생
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
	}

	@Nested
	@DisplayName("로그인 기능")
	class LoginTest {

		@Test
		@DisplayName("성공: 올바른 아이디와 비밀번호로 로그인 시, 토큰을 발급하고 Redis에 Refresh Token을 저장한다.")
		void login_Success() {
			// given (준비)
			LoginRequest request = new LoginRequest("testuser", "password123!");
			User mockUser = User.builder()
				.userId(1L) // AuthService에서 user.getUserId()를 사용하므로 이 필드가 필요합니다.
				.username("testuser")
				.password("encodedPassword") // DB에는 암호화된 비밀번호가 저장되어 있음
				.build();

			// Mock 객체들의 동작을 정의
			given(userRepository.findByUsername(request.username())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.matches(request.password(), mockUser.getPassword())).willReturn(true);
			given(jwtTokenProvider.createAccessToken(mockUser)).willReturn("dummy-access-token");
			given(jwtTokenProvider.createRefreshToken(mockUser)).willReturn("dummy-refresh-token");
			given(jwtTokenProvider.getRefreshTokenValidityInMilliseconds()).willReturn(1209600000L);

			// when (실행)
			LoginResponse response = authService.login(request);

			// then (검증)
			assertThat(response).isNotNull();
			assertThat(response.accessToken()).isEqualTo("dummy-access-token");
			assertThat(response.refreshToken()).isEqualTo("dummy-refresh-token");

			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
			ArgumentCaptor<TimeUnit> timeUnitCaptor = ArgumentCaptor.forClass(TimeUnit.class);

			// Redis에 Refresh Token이 저장되는지 검증하면서, 전달된 인자들을 캡처
			then(valueOperations).should().set(
				keyCaptor.capture(),
				valueCaptor.capture(),
				timeoutCaptor.capture(),
				timeUnitCaptor.capture()
			);

			// [확인] 캡처된 값들을 직접 출력하여 눈으로 확인합니다.
			System.out.println("=========================================");
			System.out.println("Redis Key: " + keyCaptor.getValue());
			System.out.println("Redis Value: " + valueCaptor.getValue());
			System.out.println("Redis TTL: " + timeoutCaptor.getValue());
			System.out.println("Redis TimeUnit: " + timeUnitCaptor.getValue());
			System.out.println("=========================================");

			// [검증 강화] 캡처된 값들이 예상과 일치하는지 개별적으로 검증합니다.
			assertThat(keyCaptor.getValue()).isEqualTo("RT:" + mockUser.getUserId());
			assertThat(valueCaptor.getValue()).isEqualTo("dummy-refresh-token");
			assertThat(timeoutCaptor.getValue()).isEqualTo(1209600000L);
			assertThat(timeUnitCaptor.getValue()).isEqualTo(TimeUnit.MILLISECONDS);
		}

		@Test
		@DisplayName("실패: 존재하지 않는 아이디로 로그인 시, GeneralException(USER_NOT_FOUND)을 던진다.")
		void login_Fail_UserNotFound() {
			// given
			LoginRequest request = new LoginRequest("nonexistentuser", "password123!");
			given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorReasonHttpStatus", ErrorStatus.USER_NOT_FOUND.getReasonHttpStatus());

			// 비밀번호 검증이나 토큰 생성 로직이 호출되지 않았는지 검증
			then(passwordEncoder).should(never()).matches(anyString(), anyString());
			then(jwtTokenProvider).should(never()).createAccessToken(any(User.class));
		}

		@Test
		@DisplayName("실패: 비밀번호가 일치하지 않을 시, GeneralException(INVALID_PASSWORD)을 던진다.")
		void login_Fail_InvalidPassword() {
			// given
			LoginRequest request = new LoginRequest("testuser", "wrongpassword");
			User mockUser = User.builder()
				.userId(1L)
				.username("testuser")
				.password("encodedPassword")
				.build();

			given(userRepository.findByUsername(request.username())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.matches(request.password(), mockUser.getPassword())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(GeneralException.class)
				.hasFieldOrPropertyWithValue("errorReasonHttpStatus",
					ErrorStatus.INVALID_PASSWORD.getReasonHttpStatus());

			// 토큰 생성이나 Redis 저장 로직이 호출되지 않았는지 검증
			then(jwtTokenProvider).should(never()).createAccessToken(any(User.class));
			then(valueOperations).should(never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
		}
	}
}