package app.domain.customer;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer/address")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 정보를 제공, 수정하는 API")
public class CustomerAddressController {
	private final CustomerAddressService customerAddressService;

	@GetMapping("/list")
	@Operation(summary = "사용자 주소지 목록 조회", description = "")
	public ApiResponse<List<GetCustomerAddressListResponse>> GetCustomerAddresses (
		@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
		@RequestBody @Valid AddCustomerAddressRequest request	) {
		if (request.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null.");
		}
		return ApiResponse.onSuccess(customerAddressService.getCustomerAddresses(request.userId()));
	}

	@PostMapping("/add")
	@Operation(summary = "사용자 주소지 등록", description = "")
	public ApiResponse<AddCustomerAddressResponse> AddCustomerAddress(
		@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
		@RequestBody @Valid AddCustomerAddressRequest request){

		// --- START: Input Validation ---
		if (request.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null.");
		}
		if (!StringUtils.hasText(request.alias())) {
			throw new IllegalArgumentException("Address alias is required.");
		}
		if (!StringUtils.hasText(request.address())) {
			throw new IllegalArgumentException("Address is required.");
		}
		if (!StringUtils.hasText(request.addressDetail())) {
			throw new IllegalArgumentException("상세 주소는 필수입니다.");
		}
		// --- END: Input Validation ---

		AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(request.userId(), request);
		return ApiResponse.onSuccess(response);
	}
}
