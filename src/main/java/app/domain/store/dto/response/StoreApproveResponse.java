package app.domain.store.dto.response;

import java.util.UUID;

public record StoreApproveResponse(
	UUID storeId,
	String storeApprovalStatus
) {

}
