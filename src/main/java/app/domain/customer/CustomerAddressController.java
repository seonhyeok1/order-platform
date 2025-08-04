package app.domain.customer;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.customer.status.CustomerSuccessStatus;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
	@Operation(summary = "/api/customer/address/list", description = "사용자 주소지 목록 조회")
	public ApiResponse<List<GetCustomerAddressListResponse>> GetCustomerAddresses (
		@AuthenticationPrincipal UserDetails principal) {
		Long userId = getUserIdFromPrincipal(principal);
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_LIST_FOUND, customerAddressService.getCustomerAddresses(userId));
	}

	@PostMapping("/add")
	@Operation(summary = "/api/customer/address/add", description = "사용자 주소지 등록")
	public ApiResponse<AddCustomerAddressResponse> AddCustomerAddress(
		@AuthenticationPrincipal UserDetails principal,
		@RequestBody @Valid AddCustomerAddressRequest request){
		Long userId = getUserIdFromPrincipal(principal);

		AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(userId, request);
		return ApiResponse.onSuccess(CustomerSuccessStatus.ADDRESS_ADDED, response);
	}

	private Long getUserIdFromPrincipal(UserDetails principal) {
		if (principal == null || !StringUtils.hasText(principal.getUsername())) {
			throw new GeneralException(app.global.apiPayload.code.status.ErrorStatus.USER_NOT_FOUND);
		}
		try {
			return Long.parseLong(principal.getUsername());
		} catch (NumberFormatException e) {
			throw new GeneralException(app.global.apiPayload.code.status.ErrorStatus._BAD_REQUEST);
		}
	}
}
