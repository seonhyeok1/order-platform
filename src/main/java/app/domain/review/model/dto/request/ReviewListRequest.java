package app.domain.review.model.dto.request;

import java.util.UUID;

public record ReviewListRequest(
	UUID storeId
) {
}
