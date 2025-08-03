package app.domain.customer.status;

import app.global.apiPayload.code.BaseCode;
import app.global.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomerSuccessStatus implements BaseCode {

	ADDRESS_LIST_FOUND(HttpStatus.OK, "ADDRESS_200", "주소 목록 조회가 완료되었습니다."),
	ADDRESS_ADDED(HttpStatus.OK, "ADDRESS_200", "주소 등록이 완료되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ReasonDTO getReason() {
		return ReasonDTO.builder()
				.isSuccess(true)
				.message(message)
				.code(code)
				.build();
	}

	@Override
	public ReasonDTO getReasonHttpStatus() {
		return ReasonDTO.builder()
				.isSuccess(true)
				.message(message)
				.code(code)
				.httpStatus(httpStatus)
				.build();
	}
}