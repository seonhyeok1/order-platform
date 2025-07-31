package app.domain.review.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GetReviewRequest(
	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId
) {

}
