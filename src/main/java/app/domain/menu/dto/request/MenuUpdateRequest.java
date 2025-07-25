package app.domain.menu.dto.request;

public record MenuUpdateRequest(
	String name,
	String address,
	String phoneNumber,
	String desc,
	Long minOrderAmount
) {
}
