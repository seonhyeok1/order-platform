package app.domain.auth;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.auth.model.dto.request.LoginRequest;
import app.domain.auth.model.dto.response.LoginResponse;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.dto.CreateUserReq;
import app.domain.customer.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	@Transactional
	public String createUser(CreateUserReq createUserReq) {

		// 1. 중복 검사 수정
		validateUserUniqueness(createUserReq);

		// 2. 비밀번호 암호화
		String encryptedPassword = passwordEncoder.encode(createUserReq.getPassword());

		// 3. 유저 등록
		User user = User.builder()
			.username(createUserReq.getUsername())
			.password(encryptedPassword)
			.email(createUserReq.getEmail())
			.nickname(createUserReq.getNickname())
			.realName(createUserReq.getRealName())
			.phoneNumber(createUserReq.getPhoneNumber())
			.userRole(createUserReq.getUserRole())
			.build();

		// 4. 유저 등록 및 예외 처리
		try {
			User savedUser = userRepository.save(user);
			return savedUser.getUserId().toString();
		} catch (DataAccessException e) {
			// 사전 검사를 통과했음에도 DB 에러가 발생한 경우 (e.g., Race Condition, DB 연결 끊김 등)
			log.error("데이터베이스에 사용자 등록을 실패했습니다.", e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		// 1. 사용자 인증 (DB에서 사용자 정보 조회)
		User user = userRepository.findByUsername(request.username())
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		// 2. 비밀번호 검증
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new GeneralException(ErrorStatus.INVALID_PASSWORD);
		}

		// 3. AccessToken, RefreshToken 생성
		String accessToken = jwtTokenProvider.createAccessToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user);

		// 4. RefreshToken을 Redis에 저장 (Key: "RT:{userId}", Value: refreshToken)
		redisTemplate.opsForValue().set(
			"RT:" + user.getUserId(),
			refreshToken,
			jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
			TimeUnit.MILLISECONDS
		);

		// 5. 토큰을 DTO에 담아 응답
		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	/**
	 * 사용자의 고유 필드(아이디, 이메일, 닉네임, 전화번호) 중복 여부 검사 - 회원가입에서만 사용
	 * @param createUserReq 회원가입 요청 DTO -> controller단에서 체크 이후 service에서
	 */
	private void validateUserUniqueness(CreateUserReq createUserReq) {
		if (userRepository.existsByUsername(createUserReq.getUsername())) {
			throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
		}
		if (userRepository.existsByEmail(createUserReq.getEmail())) {
			throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
		}
		if (userRepository.existsByNickname(createUserReq.getNickname())) {
			throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_EXISTS);
		}
		if (userRepository.existsByPhoneNumber(createUserReq.getPhoneNumber())) {
			throw new GeneralException(ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
		}
	}
}