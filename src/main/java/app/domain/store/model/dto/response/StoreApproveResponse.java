package app.domain.store.model.dto.response;

import java.util.UUID;

public record StoreApproveResponse(
	UUID storeId,
	String storeApprovalStatus
) {

}
