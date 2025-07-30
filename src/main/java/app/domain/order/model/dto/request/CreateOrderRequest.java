package app.domain.order.model.dto.request;

import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

	@NotNull
	@Schema(description = "결제 방법", example = "CREDIT_CARD", allowableValues = {"CREDIT_CARD", "SIMPLE_PAY",
		"BANK_TRANSFER", "CASH"})
	PaymentMethod paymentMethod,

	@NotNull
	@Schema(description = "주문 채널", example = "ONLINE", allowableValues = {"OFFLINE", "ONLINE"})
	OrderChannel orderChannel,

	@NotNull
	@Schema(description = "수령 방법", example = "DELIVERY", allowableValues = {"DELIVERY", "TAKE_OUT", "TAKE_IN"})
	ReceiptMethod receiptMethod,

	@Schema(description = "요청 메시지", example = "문 앞에 놓아주세요")
	String requestMessage,

	@NotNull
	@Min(value = 1, message = "총 금액은 양의 정수여야 합니다.")
	@Schema(description = "총 가격", example = "10000")
	Long totalPrice,

	@NotBlank
	@Schema(description = "배달 주소", example = "경기도 고양시 덕양구 ***")
	String deliveryAddress) {

}
