package app.domain.payment.model.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRequest {

	@Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	@NotNull(message = "주문 ID는 필수입니다.")
	private UUID orderId;

	@Schema(description = "취소 사유", example = "구매자가 취소를 원함")
	private String cancelReason;
}