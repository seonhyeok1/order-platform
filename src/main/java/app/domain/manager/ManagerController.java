package app.domain.manager;

import static org.springframework.data.domain.Sort.Direction.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.manager.dto.response.GetCustomListResponse;
import app.domain.manager.dto.response.GetCustomerDetailResponse;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자의 사용자 관리 API")
public class ManagerController {

	private final ManagerService managerService;

	@GetMapping
	@Operation(
		summary = "전체 사용자 목록 조회",
		description = "가입한 사용자 목록을 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬할 수 있습니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "사용자 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON200",
					    "message": "success",
					    "result": {
					        "content": [
					            {
					                "userId": 1,
					                "email": "user1@example.com",
					                "nickname": "사용자1",
					                "createdAt": "2024-01-01T10:00:00"
					            },
					            {
					                "userId": 2,
					                "email": "user2@example.com",
					                "nickname": "사용자2",
					                "createdAt": "2024-01-02T11:00:00"
					            },
					            ...
					        ],
					        "page": 0,
					        "size": 20,
					        "totalElements": 100,
					        "totalPages": 5
					    },
					}
					""")
			)
		)
	})
	public ApiResponse<PagedResponse<GetCustomListResponse>> getAllCustomer(
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
	) {
		return ApiResponse.onSuccess(managerService.getAllCustomer(pageable));
	}

	@GetMapping("/{userId}")
	@Operation(
		summary = "선택한 유저 정보 조회",
		description = "선택한 유저의 자세한 정보와 등록한 주소를 확인 합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "사용자 상세 정보 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					           {
					               "resultCode": COMMON200,
					               "message": success,
					               "result": {
					                   "userId": 1,
					                   "email": "user1@example.com",
					                   "nickname": "사용자1",
					                   "createdAt": "2024-01-01T10:00:00",
					                   "addresses": [
					                       {
					                           "addressId": 100,
					                           "alias":
					                           "address": "서울시 강남구 테헤란로 1",
					                           "isDefault": true
					                       },
					                       ...
					                   ]
					               }
					           }
					""")
			)
		)
	})
	public ApiResponse<GetCustomerDetailResponse> getUsersDetailById(
		@PathVariable("userId") Long userId
	) {
		return ApiResponse.onSuccess(managerService.getCustomerDetailById(userId));
	}

	@GetMapping("/{userId}/order")
	@Operation(
		summary = "선택한 사용자 주문내역 조회",
		description = "선택한 사용자의 주문 정보를 확인 합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "사용자 주문 내역 조회 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(value = """
					{
					  "code": COMMON200,
					  "message": "요청에 성공하였습니다.",
					  "data": {
					    "contents": [
					      {
					        "ordersId": "94d3d03a-6fd9-4c86-9cb4-188fc89d1b9e",
					        "storeName": "한솥도시락 종로점",
					        "totalPrice": 13500,
					        "deliveryAddress": "서울특별시 종로구 종로 1",
					        "paymentMethod": "CARD",
					        "receiptMethod": "DELIVERY",
					        "orderStatus": "DELIVERED",
					        "isRefundable": false,
					        "requestMessage": "문 앞에 놔주세요",
					        "createdAt": "2025-07-28T14:35:00"
					      }
					    ],
					    "totalPages": 1,
					    "totalElements": 1,
					    "page": 0,
					    "size": 20
					  }
					}
					""")
			)
		)
	})
	public ApiResponse<PagedResponse<OrderDetailResponse>> getCustomerOrderListById(
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable,
		@PathVariable("userId") Long userId
	) {
		return ApiResponse.onSuccess(managerService.getCustomerOrderListById(userId, pageable));
	}

	@GetMapping("/search")
	@Operation(
		summary = "사용자 검색",
		description = "키워드를 사용하여 가입한 사용자를 검색하고, 결과를 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬할 수 있습니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "사용자 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON200",
					    "message": "success",
					    "result": {
					        "content": [
					            {
					                "userId": 1,
					                "name":"홍길동",
					                "email": "user1@example.com",
					                "createdAt": "2024-01-01T10:00:00"
					            },
					            {
					                "userId": 2,
					                "name": "사용자2",
					                "email": "user2@example.com",
					                "createdAt": "2024-01-02T11:00:00"
					            }
					        ],
					        "page": 0,
					        "size": 20,
					        "totalElements": 100,
					        "totalPages": 5
					    },
					}
					""")
			)
		)
	})
	public ApiResponse<PagedResponse<GetCustomListResponse>> searchCustomer(
		Pageable pageable,
		@RequestParam String keyWord
	) {
		return ApiResponse.onSuccess(managerService.searchCustomer(keyWord, pageable));
	}
}