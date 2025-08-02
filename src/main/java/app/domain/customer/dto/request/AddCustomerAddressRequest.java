package app.domain.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddCustomerAddressRequest {
	private String alias;
	private String address;
	private String addressDetail;
	private boolean isDefault;
}