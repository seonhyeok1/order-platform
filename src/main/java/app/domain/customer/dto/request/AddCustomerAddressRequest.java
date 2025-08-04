package app.domain.customer.dto.request;

public record AddCustomerAddressRequest(
	String alias,
	String address,
	String addressDetail,
	boolean isDefault
) {
}