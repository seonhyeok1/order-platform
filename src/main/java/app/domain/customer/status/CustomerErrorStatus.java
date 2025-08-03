package app.domain.customer.status;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomerErrorStatus implements BaseCode {

	ADDRESS_ALREADY_EXISTS(HttpStatus.CONFLICT, "ADDRESS_001", "이미 존재하는 주소입니다."),
	ADDRESS_ADD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ADDRESS_002", "주소 등록에 실패했습니다."),
	ADDRESS_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ADDRESS_004", "주소 목록 조회에 실패했습니다.");

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
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
				.message(message)
				.code(code)
				.httpStatus(httpStatus)
				.build();
	}
}