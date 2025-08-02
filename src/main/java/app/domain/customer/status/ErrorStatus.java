package app.domain.customer.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseErrorCode;
import app.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

	// 인증 관련
	INVALID_PASSWORD(HttpStatus.FORBIDDEN, "AUTH_001", "비밀번호가 일치하지 않습니다."),

	// User 관련
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "존재하지 않는 사용자입니다."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER002", "이미 존재하는 유저입니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER003", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER004", "이미 사용 중인 닉네임입니다."),
	PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER005", "이미 사용 중인 전화번호입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ErrorReasonDTO getReason() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ErrorReasonDTO getReasonHttpStatus() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}
