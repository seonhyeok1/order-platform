package app.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import app.global.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"resultCode", "message", "result"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private String resultCode;
	private String message;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T result;

	// 성공 응답
	public static <T> ApiResponse<T> onSuccess(T result) {
		return new ApiResponse<>(SuccessStatus._OK.getCode(), "success", result);
	}

	// 실패 응답
	public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
		return new ApiResponse<>(code, message, data);
	}
}