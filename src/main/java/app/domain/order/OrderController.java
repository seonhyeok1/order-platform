package app.domain.order;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.order.model.dto.request.CreateOrderRequest;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "order", description = "주문 관련 API")
@RestController
@RequestMapping("/customer/order")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "주문 생성 API", description = "사용자의 장바구니를 기반으로 주문을 생성합니다.")
	@PostMapping
	public ApiResponse<UUID> createOrder(
		@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
		@Valid @RequestBody CreateOrderRequest request) {
		Long userId = Long.parseLong(principal.getUsername());
		UUID orderId = orderService.createOrder(userId, request);
		return ApiResponse.onSuccess(orderId);
	}

	@Operation(summary = "주문 상세 조회 API", description = "주문 ID로 주문 상세 정보를 조회합니다.")
	@GetMapping("/{orderId}")
	public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable UUID orderId) {
		OrderDetailResponse result = orderService.getOrderDetail(orderId);
		return ApiResponse.onSuccess(result);
	}

}
