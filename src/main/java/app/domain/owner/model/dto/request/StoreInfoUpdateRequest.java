package app.domain.owner.model.dto.request;

public record StoreInfoUpdateRequest(
	String name,
	String address,
	String phoneNumber,
	String minOrderAmount,
	String desc
) {
}
