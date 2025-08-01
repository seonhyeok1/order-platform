package app.domain.payment.model.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "결제 실패 처리 요청")
public record PaymentFailRequest(
    @Schema(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "주문 ID는 필수입니다.")
    UUID orderId,
    
    @Schema(description = "에러 코드", example = "INVALID_CARD")
    @NotBlank(message = "에러 코드는 필수입니다.")
    String errorCode,
    
    @Schema(description = "실패 사유", example = "유효하지 않은 카드입니다.")
    @NotBlank(message = "실패 사유는 필수입니다.")
    String message
) {
}