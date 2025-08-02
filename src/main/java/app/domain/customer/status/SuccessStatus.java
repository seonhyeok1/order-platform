package app.domain.customer.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseErrorCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseErrorCode {

	// User
	SIGNUP_OK(HttpStatus.OK, "SIGNUP200", "회원가입이 완료되었습니다."),
	LOGIN_OK(HttpStatus.OK, "LOGIN200", "로그인 되었습니다."),
	LOGOUT_OK(HttpStatus.OK, "LOGOUT200", "로그아웃 되었습니다."),
	WITHDRAW_OK(HttpStatus.OK, "WITHDRAW200", "회원 탈퇴가 완료되었습니다."),

	// Email
	EMAIL_OK(HttpStatus.OK, "EMAILSEND200", "인증번호 전송되었습니다"),
	VERIFY_OK(HttpStatus.OK, "VERIFY200", "이메일 인증이 완료되었습니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.build();
	}

	@Override
	public ReasonDTO getReasonHttpStauts() {
		return ReasonDTO.builder()
			.message(message)
			.code(code)
			.httpStatus(httpStatus)
			.build();
	}
}
