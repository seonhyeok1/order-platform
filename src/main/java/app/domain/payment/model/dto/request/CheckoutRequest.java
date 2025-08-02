package app.domain.payment.model.dto.request;

import java.util.UUID;

import app.domain.order.model.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutRequest(

	@NotNull(message = "주문 ID는 필수입니다.")
	UUID orderId,

	@NotNull(message = "총 결제 금액은 필수입니다.")
	@Positive(message = "총 결제 금액은 양수여야 합니다.")
	Long totalPrice,

	@NotNull(message = "결제 수단은 필수입니다.")
	PaymentMethod paymentMethod
) {
}