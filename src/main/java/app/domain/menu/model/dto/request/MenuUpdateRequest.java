package app.domain.menu.model.dto.request;

public record MenuUpdateRequest(
	String name,
	String address,
	String phoneNumber,
	String desc,
	Long minOrderAmount
) {
}
