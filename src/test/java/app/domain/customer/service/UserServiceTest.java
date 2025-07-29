package app.domain.customer.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.domain.auth.service.impl.AuthService;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.dto.CreateUserReq;
import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능을 JUnit 5와 함께 사용
class UserServiceTest {

	@InjectMocks // 테스트 대상 클래스. @Mock으로 생성된 객체들이 자동으로 주입됩니다.
	private AuthService userService;

	@Mock // 테스트 대상 클래스가 의존하는 객체들을 가짜(Mock) 객체로 생성
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	/*-------------------------------------------
	 *
	 *        회원가입 실패 - 이미 존재하는 아이디
	 *
	 *-------------------------------------------*/

	@Test
	@DisplayName("회원가입 실패 - 이미 존재하는 아이디 (userName 중복 오류)")
	void createUser_fail_when_username_already_exists() {
		// given
		CreateUserReq request = new CreateUserReq();
		request.setUsername("existingUser");
		request.setPassword("password123");

		// given: findByUserName이 호출되면, 빌더로 생성한 User 객체를 포함한 Optional을 반환
		given(userRepository.findByUsername("existingUser")).willReturn(Optional.of(User.builder().build()));

		// when then
		// userService.createUser(request)를 실행했을 때 GeneralException이 발생하는지 확인
		GeneralException exception = assertThrows(GeneralException.class, () -> {
			userService.createUser(request);
		});

		// 발생한 예외의 에러 코드가 USER_ALREADY_EXISTS인지 확인
		assertThat(exception.getErrorStatus()).isEqualTo(
			ErrorStatus.USER_ALREADY_EXISTS); // GeneralException의 필드명에 맞게 수정
	}

	/*-------------------------------------------
	 *
	 *                 회원가입 성공
	 *
	 *-------------------------------------------*/

	@Test
	@DisplayName("회원가입 성공")
	void createUser_success() {
		// given
		CreateUserReq request = new CreateUserReq();
		request.setUsername("newUser");
		request.setPassword("password123");
		request.setEmail("newuser@example.com");
		request.setNickname("newbie");
		request.setPhoneNumber("010-1234-5678");

		// given: DB에 저장된 후 반환될 User 객체를 미리 정의 (ID가 1L로 설정됨)
		Long newUserId = 1L;
		User savedUser = User.builder()
			.userId(newUserId) // ID는 Long 타입
			.username(request.getUsername())
			.password("encryptedPassword")
			.email(request.getEmail())
			.nickname(request.getNickname())
			.phoneNumber(request.getPhoneNumber())
			.userRole(UserRole.CUSTOMER)
			.build();

		// given: Mock 객체들의 동작을 정의
		given(userRepository.findByUsername(request.getUsername())).willReturn(Optional.empty());
		given(passwordEncoder.encode(request.getPassword())).willReturn("encryptedPassword");
		given(userRepository.save(any(User.class))).willReturn(savedUser);

		// when (실행)
		String resultUserId = userService.createUser(request);

		// then (검증)
		assertThat(resultUserId).isEqualTo(newUserId.toString());
		// userRepository의 save 메서드가 User 클래스의 어떤 인스턴스로든 한 번 호출되었는지 검증
		verify(userRepository).save(any(User.class));
	}
}