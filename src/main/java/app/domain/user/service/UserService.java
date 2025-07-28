package app.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.user.model.UserRepository;
import app.domain.user.model.dto.CreateUserReq;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public String createUser(CreateUserReq createUserReq) {

		// 1. userName 중복 확인
		if (userRepository.findByUserName(createUserReq.getUserName()).isPresent()) {
			throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
		}

		// 2. 비밀번호 암호화
		String encryptedPassword = passwordEncoder.encode(createUserReq.getPassword());

		// 3. 유저 등록
		User user = User.builder()
			.username(createUserReq.getUserName())
			.password(encryptedPassword)
			.email(createUserReq.getEmail())
			.nickname(createUserReq.getNickname())
			.phoneNumber(createUserReq.getPhoneNumber())
			.userRole(UserRole.CUSTOMER)
			.build();

		User savedUser = userRepository.save(user);

		return savedUser.getUserId().toString();
	}
}