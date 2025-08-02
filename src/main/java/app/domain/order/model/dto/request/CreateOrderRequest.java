package app.domain.order.model.dto.request;

import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

	@NotNull
	private PaymentMethod paymentMethod;

	@NotNull
	private OrderChannel orderChannel;

	@NotNull
	private ReceiptMethod receiptMethod;

	private String requestMessage;

	@NotNull
	@Positive(message = "총 금액은 양의 정수여야 합니다.")
	private Long totalPrice;

	@NotBlank
	private String deliveryAddress;
}
