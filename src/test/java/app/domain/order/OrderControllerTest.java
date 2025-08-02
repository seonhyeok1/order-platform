package app.domain.order;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.order.model.dto.request.CreateOrderRequest;
import app.domain.order.model.dto.response.OrderDetailResponse;
import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.SecurityConfig;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
@DisplayName("OrderController 테스트")
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Test
	@DisplayName("주문 생성 - 성공")
	@WithMockUser
	void createOrder_Success() throws Exception {
		Long userId = 1L;
		UUID orderId = UUID.randomUUID();
		CreateOrderRequest request = new CreateOrderRequest(
			PaymentMethod.CREDIT_CARD,
			OrderChannel.ONLINE,
			ReceiptMethod.DELIVERY,
			"문 앞에 놓아주세요",
			10000L,
			"서울시 강남구"
		);

		when(orderService.createOrder(eq(userId), any(CreateOrderRequest.class)))
			.thenReturn(orderId);

		mockMvc.perform(post("/customer/order")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value(orderId));

		verify(orderService).createOrder(eq(userId), any(CreateOrderRequest.class));
	}

	@Test
	@DisplayName("주문 생성 - 총 금액 0 이하 실패")
	@WithMockUser
	void createOrder_InvalidTotalPrice() throws Exception {
		Long userId = 1L;
		CreateOrderRequest request = new CreateOrderRequest(
			PaymentMethod.CREDIT_CARD,
			OrderChannel.ONLINE,
			ReceiptMethod.DELIVERY,
			"문 앞에 놓아주세요",
			0L,
			"서울시 강남구"
		);

		mockMvc.perform(post("/customer/order")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.resultCode").value("ORDER005"))
			.andExpect(jsonPath("$.message").value("총 금액은 양의 정수입니다."));

		verify(orderService, never()).createOrder(any(), any());
	}

	@Test
	@DisplayName("주문 생성 - 서비스 에러")
	@WithMockUser
	void createOrder_ServiceError() throws Exception {
		Long userId = 1L;
		CreateOrderRequest request = new CreateOrderRequest(
			PaymentMethod.CREDIT_CARD,
			OrderChannel.ONLINE,
			ReceiptMethod.DELIVERY,
			"문 앞에 놓아주세요",
			10000L,
			"서울시 강남구"
		);

		when(orderService.createOrder(eq(userId), any(CreateOrderRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_CREATE_FAILED));

		mockMvc.perform(post("/customer/order")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.resultCode").value("ORDER001"))
			.andExpect(jsonPath("$.message").value("주문 생성에 실패했습니다."));
	}

	@Test
	@DisplayName("주문 상세 조회 - 성공")
	@WithMockUser
	void getOrderDetail_Success() throws Exception {
		UUID orderId = UUID.randomUUID();
		OrderDetailResponse response = new OrderDetailResponse(
			"맛있는 치킨집",
			List.of(new OrderDetailResponse.Menu("후라이드 치킨", 2, 18000)),
			36000L,
			"서울시 강남구",
			PaymentMethod.CREDIT_CARD,
			OrderChannel.ONLINE,
			ReceiptMethod.DELIVERY,
			OrderStatus.PENDING,
			"문 앞에 놓아주세요"
		);

		when(orderService.getOrderDetail(orderId)).thenReturn(response);

		mockMvc.perform(get("/customer/order/{orderId}", orderId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result.storeName").value("맛있는 치킨집"))
			.andExpect(jsonPath("$.result.menuList").isArray())
			.andExpect(jsonPath("$.result.menuList.length()").value(1))
			.andExpect(jsonPath("$.result.menuList[0].menuName").value("후라이드 치킨"))
			.andExpect(jsonPath("$.result.menuList[0].quantity").value(2))
			.andExpect(jsonPath("$.result.menuList[0].price").value(18000))
			.andExpect(jsonPath("$.result.totalPrice").value(36000))
			.andExpect(jsonPath("$.result.deliveryAddress").value("서울시 강남구"))
			.andExpect(jsonPath("$.result.paymentMethod").value("CREDIT_CARD"))
			.andExpect(jsonPath("$.result.orderChannel").value("ONLINE"))
			.andExpect(jsonPath("$.result.receiptMethod").value("DELIVERY"))
			.andExpect(jsonPath("$.result.orderStatus").value("PENDING"))
			.andExpect(jsonPath("$.result.requestMessage").value("문 앞에 놓아주세요"));

		verify(orderService).getOrderDetail(orderId);
	}

	@Test
	@DisplayName("주문 상세 조회 - 주문을 찾을 수 없음")
	@WithMockUser
	void getOrderDetail_OrderNotFound() throws Exception {
		UUID orderId = UUID.randomUUID();

		when(orderService.getOrderDetail(orderId))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		mockMvc.perform(get("/customer/order/{orderId}", orderId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.resultCode").value("ORDER006"))
			.andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));

		verify(orderService).getOrderDetail(orderId);
	}

	@Test
	@DisplayName("주문 상세 조회 - 서버 에러")
	@WithMockUser
	void getOrderDetail_ServerError() throws Exception {
		UUID orderId = UUID.randomUUID();

		when(orderService.getOrderDetail(orderId))
			.thenThrow(new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR));

		mockMvc.perform(get("/customer/order/{orderId}", orderId))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.resultCode").value("COMMON500"))
			.andExpect(jsonPath("$.message").value("서버 에러, 관리자에게 문의 바랍니다."));

		verify(orderService).getOrderDetail(orderId);
	}

	@Test
	@DisplayName("주문 생성 - 잘못된 JSON 형식")
	@WithMockUser
	void createOrder_InvalidJson() throws Exception {
		Long userId = 1L;
		String invalidJson = "{\"paymentMethod\": \"INVALID_METHOD\", \"totalPrice\": 10000}";

		mockMvc.perform(post("/customer/order")
				.param("userId", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andExpect(status().isBadRequest());

		verify(orderService, never()).createOrder(any(), any());
	}

	@Test
	@DisplayName("주문 생성 - 필수 파라미터 누락")
	@WithMockUser
	void createOrder_MissingUserId() throws Exception {
		CreateOrderRequest request = new CreateOrderRequest(
			PaymentMethod.CREDIT_CARD,
			OrderChannel.ONLINE,
			ReceiptMethod.DELIVERY,
			"문 앞에 놓아주세요",
			10000L,
			"서울시 강남구"
		);

		mockMvc.perform(post("/customer/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(orderService, never()).createOrder(any(), any());
	}

	@Test
	@DisplayName("주문 상세 조회 - 잘못된 UUID 형식")
	@WithMockUser
	void getOrderDetail_InvalidUUID() throws Exception {
		String invalidUUID = "invalid-uuid";

		mockMvc.perform(get("/customer/order/{orderId}", invalidUUID))
			.andExpect(status().isBadRequest());

		verify(orderService, never()).getOrderDetail(any());
	}
}