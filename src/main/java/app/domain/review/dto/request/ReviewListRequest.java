package app.domain.review.dto.request;

import java.util.UUID;

public record ReviewListRequest(
	UUID storeId
) {
}
