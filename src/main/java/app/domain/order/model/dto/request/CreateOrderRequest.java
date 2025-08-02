package app.domain.order.model.dto.request;

import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

	@NotNull
	PaymentMethod paymentMethod,

	@NotNull
	OrderChannel orderChannel,

	@NotNull
	ReceiptMethod receiptMethod,

	String requestMessage,

	@NotNull
	@Min(value = 1, message = "총 금액은 양의 정수여야 합니다.")
	Long totalPrice,

	@NotBlank
	String deliveryAddress) {

}
