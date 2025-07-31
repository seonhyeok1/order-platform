package app.domain.review.model.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateReviewRequest(
	@Schema(description = "주문 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	@NotNull(message = "주문 ID는 필수입니다.")
	UUID orderId,

	@Schema(description = "평점", example = "5")
	@NotNull(message = "평점은 필수입니다.")
	@Min(value = 1, message = "평점은 1 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 5 이하여야 합니다.")
	Long rating,

	@Schema(description = "리뷰 내용", example = "맛있어요!")
	@NotBlank(message = "리뷰 내용은 필수입니다.")
	String content
) {

}
