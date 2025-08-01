package app.domain.payment.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "결제 승인 요청")
public record PaymentConfirmRequest(
	@Schema(description = "결제 키", example = "tviva20240101000001")
	@NotBlank(message = "결제 키는 필수입니다.")
	String paymentKey,
	
	@Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotBlank(message = "주문 ID는 필수입니다.")
	String orderId,
	
	@Schema(description = "결제 금액", example = "50000")
	@NotBlank(message = "결제 금액은 필수입니다.")
	@Pattern(regexp = "^[1-9]\\d*$", message = "결제 금액은 양수여야 합니다.")
	String amount
) {
}