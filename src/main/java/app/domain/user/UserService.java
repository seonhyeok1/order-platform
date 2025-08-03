package app.domain.user;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.cart.model.CartRepository;
import app.domain.cart.model.entity.Cart;
import app.domain.user.model.UserRepository;
import app.domain.user.model.dto.CreateUserReq;
import app.domain.user.model.dto.request.LoginRequest;
import app.domain.user.model.dto.response.LoginResponse;
import app.domain.user.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final CartRepository cartRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private static final String BLACKLIST_PREFIX = "BL:";

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
			cartRepository.save(Cart.builder().user(savedUser).build());
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
			REFRESH_TOKEN_PREFIX + user.getUserId(),
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

	@Transactional
	public void logout() {
		// 1. SecurityContext에서 인증 정보 가져오기
		// SecurityConfig에서 .authenticated()로 보호되므로, 인증 정보가 없으면 이 코드에 도달할 수 없음
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 2. 인증 정보에서 사용자 ID와 Access Token 추출
		// authentication.getName()은 JwtTokenProvider에서 토큰의 subject로 설정한 사용자 ID임
		String userId = authentication.getName();
		// authentication.getCredentials()는 JwtTokenProvider에서 토큰 자체를 저장해두었음
		String accessToken = (String)authentication.getCredentials();

		// 3. Redis에서 해당 유저의 Refresh Token 삭제 (핵심 로그아웃 로직)
		String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
		if (Boolean.TRUE.equals(redisTemplate.hasKey(refreshTokenKey))) {
			redisTemplate.delete(refreshTokenKey);
		}

		// 4. Access Token을 블랙리스트에 추가 (보안 강화 로직)
		// 남은 유효 시간을 계산하여 TTL로 설정
		Long expiration = jwtTokenProvider.getExpiration(accessToken);
		if (expiration > 0) { // 만료되지 않은 토큰만 블랙리스트에 추가
			redisTemplate.opsForValue().set(
				BLACKLIST_PREFIX + accessToken,
				"logout",
				expiration,
				TimeUnit.MILLISECONDS
			);
		}
	}

	@Transactional
	public void withdrawMembership() {
		// 1. 현재 인증된 사용자 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long currentUserId = Long.parseLong(authentication.getName());

		// 2. DB에서 사용자 정보 조회
		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		// 3. 개인정보 익명화 처리
		user.anonymizeForWithdrawal();

		// 4. Soft Delete 수행
		userRepository.delete(user);

		// 5. 현재 세션 무효화 (로그아웃 처리 코드 재사용)
		logout();
	}

	/**
	 * 사용자의 고유 필드(아이디, 이메일, 닉네임, 전화번호) 중복 여부 검사 - 회원가입에서만 사용
	 * @param createUserReq 회원가입 요청 DTO
	 */
	private void validateUserUniqueness(CreateUserReq createUserReq) {
		userRepository.findFirstByUniqueFields(
			createUserReq.getUsername(),
			createUserReq.getEmail(),
			createUserReq.getNickname(),
			createUserReq.getPhoneNumber()
		).ifPresent(user -> { // 중복된 사용자가 존재하면
			if (user.getUsername().equals(createUserReq.getUsername())) {
				throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
			}
			if (user.getEmail().equals(createUserReq.getEmail())) {
				throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
			}
			if (user.getNickname().equals(createUserReq.getNickname())) {
				throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_EXISTS);
			}
			if (user.getPhoneNumber().equals(createUserReq.getPhoneNumber())) {
				throw new GeneralException(ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
			}
		});
	}
}