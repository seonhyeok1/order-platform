package app.domain.customer.dto.response;

import java.util.UUID;

public record AddCustomerAddressResponse(
	UUID address_id
) {
}