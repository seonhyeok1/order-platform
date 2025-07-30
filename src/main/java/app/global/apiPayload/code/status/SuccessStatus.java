package app.global.apiPayload.code.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuccessStatus implements BaseCode {
	// 일반적인 응답
	_OK(HttpStatus.OK, "COMMON200", "success"),

	// Email 관련 응답
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
