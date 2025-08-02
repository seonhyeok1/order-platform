package app.domain.payment.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRequest {

	@NotNull(message = "주문 ID는 필수입니다.")
	private UUID orderId;

	private String cancelReason;
}