package app.domain.customer;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.http.ResponseEntity;
//import app.global.apiPayload.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;

import java.util.UUID;

import app.domain.customer.model.dto.request.AddUserAddressRequest;
import app.domain.customer.model.dto.response.AddUserAddressResponse;



@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name="사용자 API", description = "사용자 정보를 제공, 수정하는 API")
public class UserController {
    private final UserAddressService userAddressService;

//    @GetMapping("/address")
//    public String GetUserAddress() {
//        return null;
//    }

    @PostMapping("/address")
    public ResponseEntity<AddUserAddressResponse> addUserAddress(
            @Valid @RequestBody AddUserAddressRequest request //JSON Body → DTO 자동 바인딩
            //@AuthenticationPrincipal CustomUserDetails userDetails, //로그인한 사용자 정보 추출 (Spring Security)
    ) {
        //Long userId = userDetails.getUserId(); // 로그인된 사용자 ID 추출
        UUID addressId = userAddressService.addAddress(request.userId(), request); //실제 주소 등록 로직은 서비스 계층에서 처리

        return ResponseEntity.ok((new AddUserAddressResponse(addressId))
        );  //	통일된 응답 포맷 사용
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
