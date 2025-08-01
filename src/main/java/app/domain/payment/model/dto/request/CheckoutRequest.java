package app.domain.payment.model.dto.request;

import java.util.UUID;

import app.domain.order.model.entity.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "결제 페이지 요청")
public record CheckoutRequest(
	@Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotNull(message = "주문 ID는 필수입니다.")
	UUID orderId,
	
	@Schema(description = "총 결제 금액", example = "50000")
	@NotNull(message = "총 결제 금액은 필수입니다.")
	@Positive(message = "총 결제 금액은 양수여야 합니다.")
	Long totalPrice,
	
	@Schema(description = "결제 수단", example = "CREDIT_CARD")
	@NotNull(message = "결제 수단은 필수입니다.")
	PaymentMethod paymentMethod
) {
}