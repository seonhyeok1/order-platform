package app.domain.payment.model.dto.request;

public record PaymentConfirmRequest(
	String paymentKey,
	String orderId,
	String amount
) {
}