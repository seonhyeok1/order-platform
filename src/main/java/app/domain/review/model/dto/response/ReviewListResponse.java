package app.domain.review.model.dto.response;

import java.sql.Array;
import java.util.UUID;

public record ReviewListResponse(
	UUID storeId,
	UUID reviewId,
	String context,
	Long rating,
	Array reviewList
) {
}
