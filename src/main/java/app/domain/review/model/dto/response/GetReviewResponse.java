package app.domain.review.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import app.domain.review.model.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetReviewResponse(
	@Schema(description = "리뷰 ID", example = "123e4567-e89b-12d3-a456-426614174001")
	UUID reviewId,
	@Schema(description = "고객 이름", example = "홍길동")
	String customerName,
	@Schema(description = "가게 이름", example = "맛있는 치킨집")
	String storeName,
	@Schema(description = "평점", example = "5")
	Integer rating,
	@Schema(description = "리뷰 내용", example = "맛있어요!")
	String content,
	@Schema(description = "작성일", example = "2024-07-31T12:00:00")
	LocalDateTime createdAt
) {
	public static GetReviewResponse from(Review review) {
		return new GetReviewResponse(
			review.get
}