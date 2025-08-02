package app.domain.customer;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
		@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
		Long userId = getUserIdFromPrincipal(principal);
		return ApiResponse.onSuccess(customerAddressService.getCustomerAddresses(userId));
	}

	@PostMapping("/add")
	@Operation(summary = "/api/customer/address/add", description = "사용자 주소지 등록")
	public ApiResponse<AddCustomerAddressResponse> AddCustomerAddress(
		@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
		@RequestBody @Valid AddCustomerAddressRequest request){
		Long userId = getUserIdFromPrincipal(principal);

		if (!StringUtils.hasText(request.getAlias())) {
			throw new IllegalArgumentException("주소 별명은 필수입니다.");
		}
		if (!StringUtils.hasText(request.getAddress())) {
			throw new IllegalArgumentException("주소는 필수입니다.");
		}
		if (!StringUtils.hasText(request.getAddressDetail())) {
			throw new IllegalArgumentException("상세 주소는 필수입니다.");
		}

		AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(userId, request);
		return ApiResponse.onSuccess(response);
	}

	private Long getUserIdFromPrincipal(org.springframework.security.core.userdetails.User principal) {
		if (principal == null || !StringUtils.hasText(principal.getUsername())) {
			throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		}
		try {
			return Long.parseLong(principal.getUsername());
		} catch (NumberFormatException e) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
	}
}
