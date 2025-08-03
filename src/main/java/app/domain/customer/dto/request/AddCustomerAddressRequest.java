package app.domain.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddCustomerAddressRequest {
	@NotBlank(message="Alias is required")
	private String alias;

	@NotBlank(message="Address is required")
	private String address;

	@NotBlank(message="AddressDetail is required")
	private String addressDetail;

	private boolean isDefault;
}