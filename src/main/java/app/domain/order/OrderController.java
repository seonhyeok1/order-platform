package app.domain.order;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.order.model.dto.request.CreateOrderRequest;
import app.domain.order.model.dto.request.UpdateOrderStatusRequest;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.domain.order.model.dto.response.UpdateOrderStatusResponse;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "order", description = "주문 관련 API")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "주문 생성 API", description = "사용자의 장바구니를 기반으로 주문을 생성합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON200",
					    "message": "success",
					    "result": "550e8400-e29b-41d4-a716-446655440000 가 생성되었습니다"
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BAD_REQUEST, 잘못된 요청",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "ORDER002",
					    "message": "유효하지 않은 주문 요청입니다."
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NOT_FOUND, 리소스를 찾을 수 없음",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "CART004",
					    "message": "장바구니를 찾을 수 없습니다."
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 주문 생성 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "ORDER001",
					    "message": "주문 생성에 실패했습니다."
					}
					""")))
	})
	@PostMapping
	public ApiResponse<String> createOrder(@RequestParam Long userId, @RequestBody CreateOrderRequest request) {
		if (request.totalPrice() <= 0) {
			throw new GeneralException(ErrorStatus.INVALID_TOTAL_PRICE);
		}
		String result = orderService.createOrder(userId, request, LocalDateTime.now());
		return ApiResponse.onSuccess(result);
	}

	@Operation(summary = "주문 상세 조회 API", description = "주문 ID로 주문 상세 정보를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON200",
					    "message": "success",
					    "result": {
					        "storeName": "맛있는 치킨집",
					        "menuList": [
					            {
					                "menuName": "후라이드 치킨",
					                "quantity": 2,
					                "price": 18000
					            }
					        ],
					        "totalPrice": 36000,
					        "deliveryAddress": "서울시 강남구",
					        "paymentMethod": "CREDIT_CARD",
					        "orderChannel": "ONLINE",
					        "receiptMethod": "DELIVERY",
					        "orderStatus": "PENDING",
					        "requestMessage": "문 앞에 놓아주세요"
					    }
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NOT_FOUND, 주문을 찾을 수 없음",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "ORDER006",
					    "message": "주문을 찾을 수 없습니다."
					}
					"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR, 서버 오류",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON500",
					    "message": "서버 에러, 관리자에게 문의 바랍니다."
					}
					""")))
	})
	@GetMapping("/{orderId}")
	public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
		OrderDetailResponse result = orderService.getOrderDetail(orderId);
		return ApiResponse.onSuccess(result);
	}

	@Operation(summary = "주문 상태 변경 API", description = "주문 ID로 주문 상태를 변경합니다.")
	@PatchMapping("/{orderId}/status")
	public ApiResponse<UpdateOrderStatusResponse> updateOrderStatus(
		@PathVariable UUID orderId,
		@Valid @RequestBody UpdateOrderStatusRequest request
	) {
		UpdateOrderStatusResponse response = orderService.updateOrderStatus(orderId, request.getNewStatus());
		return ApiResponse.onSuccess(response);
	}

}
