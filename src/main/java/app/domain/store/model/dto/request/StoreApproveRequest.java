package app.domain.store.model.dto.request;

import java.util.UUID;

public record StoreApproveRequest(
	UUID userId,
	UUID regionId,
	String address,
	String storeName,
	String desc,
	String phoneNumber,
	Long minOrderAmount
) {

}
