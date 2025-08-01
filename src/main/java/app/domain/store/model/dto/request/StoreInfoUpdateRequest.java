package app.domain.store.model.dto.request;

import java.util.UUID;

public record StoreInfoUpdateRequest(
	UUID storeId,
	UUID categoryId,
	String name,
	String address,
	String phoneNumber,
	Long minOrderAmount,
	String desc
) {
}
