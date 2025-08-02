package app.domain.payment.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentFailRequest(
	@NotNull(message = "주문 ID는 필수입니다.")
	UUID orderId,

	@NotBlank(message = "에러 코드는 필수입니다.")
	String errorCode,

	@NotBlank(message = "실패 사유는 필수입니다.")
	String message
) {
}