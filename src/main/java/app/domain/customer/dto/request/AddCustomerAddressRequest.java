package app.domain.customer.dto.request;

public record AddCustomerAddressRequest(
	Long userId,
	String alias,
	String address,
	String addressDetail,
	boolean isDefault
) {
}