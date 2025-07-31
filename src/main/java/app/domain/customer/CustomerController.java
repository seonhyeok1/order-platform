package app.domain.customer;

import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 정보를 제공, 수정하는 API")
public class CustomerController {
	private final CustomerAddressService customerAddressService;

	//    @GetMapping("/address")
	//    public String GetUserAddress() {
	//        return null;
	//    }

	@PostMapping("/address") // 사용자가 접속하면 메서드 실행
	@Operation(summary = "사용자 주소지 등록", description = "")
	public ApiResponse<AddCustomerAddressResponse> AddUserAddress(
		@RequestBody @Valid AddCustomerAddressRequest request) {
		AddCustomerAddressResponse response = customerAddressService.addUserAddress(request);
		return ApiResponse.onSuccess(response);
	}

	//    @GetMapping("/review")
	//    public String GetUserReview() {
	//        return null;
	//    }
	//
	//    @PostMapping("/review")
	//    public String AddUserReview() {
	//        return null;
	//    }

	//    @GetMapping("/order")
	//    public String GetUserOrder() {
	//        return null;
	//    }

}
