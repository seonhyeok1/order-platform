package app.domain.auth.service.impl;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.customer.model.UserRepository;
import app.domain.customer.model.dto.CreateUserReq;
import app.domain.customer.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public String createUser(CreateUserReq createUserReq) {

		// 1. userName 중복 확인
		if (userRepository.findByUsername(createUserReq.getUsername()).isPresent()) {
			throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
		}

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

		try {
			User savedUser = userRepository.save(user);
			return savedUser.getUserId().toString();
		} catch (DataIntegrityViolationException e) {
			// 1. Unique 제약조건 위반 예외를 먼저 처리
			log.error("데이터베이스 Unique 제약조건 위반", e);

			// NullPointerException 방지를 위해 Optional 사용
			String rootMsg = Optional.ofNullable(e.getRootCause())
				.map(Throwable::getMessage)
				.orElse("");

			// 2. 예외 메시지에 포함된 DB 제약조건 이름을 분석하여 원인 파악
			if (rootMsg.contains("p_user_email_key")) {
				throw new GeneralException(ErrorStatus.EMAIL_ALREADY_EXISTS);
			} else if (rootMsg.contains("p_user_nickname_key")) {
				throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_EXISTS);
			} else if (rootMsg.contains("p_user_phone_number_key")) {
				throw new GeneralException(ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS);
			}
			// 분석되지 않은 다른 무결성 제약조건 위반은 일반 서버 에러로 처리
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);

		} catch (DataAccessException e) {
			// 3. 그 외의 모든 데이터베이스 관련 예외 처리
			log.error("데이터베이스에 사용자 등록을 실패했습니다.", e);
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}