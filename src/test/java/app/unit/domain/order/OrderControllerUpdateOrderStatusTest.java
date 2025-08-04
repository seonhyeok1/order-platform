package app.unit.domain.order;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.order.OrderController;
import app.domain.order.model.dto.request.UpdateOrderStatusRequest;
import app.domain.order.model.dto.response.UpdateOrderStatusResponse;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.order.service.OrderService;
import app.domain.order.status.OrderErrorStatus;
import app.domain.order.status.OrderSuccessStatus;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.MockSecurityConfig;

@WebMvcTest(controllers = OrderController.class)
@Import(MockSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrderController 테스트")
class OrderControllerUpdateOrderStatusTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Nested
	@DisplayName("주문 상태 변경 API [/customer/order/{orderId}/status] 테스트")
	class UpdateOrderStatusTest {

		@Test
		@DisplayName("성공: 유효한 요청으로 주문 상태를 변경하면 200 OK와 변경된 상태 정보를 반환한다.")
		void updateOrderStatus_Success() throws Exception {
			// given
			UUID orderId = UUID.randomUUID();
			UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
			request.setNewStatus(OrderStatus.ACCEPTED);

			UpdateOrderStatusResponse mockResponse = UpdateOrderStatusResponse.builder()
				.orderId(orderId)
				.updatedStatus(OrderStatus.ACCEPTED)
				.build();

			given(orderService.updateOrderStatus(eq(orderId), eq(OrderStatus.ACCEPTED))).willReturn(mockResponse);

			// when
			ResultActions resultActions = mockMvc.perform(patch("/customer/order/{orderId}/status", orderId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.code").value(OrderSuccessStatus.ORDER_STATUS_UPDATED.getCode()))
				.andExpect(jsonPath("$.result.orderId").value(orderId.toString()))
				.andExpect(jsonPath("$.result.updatedStatus").value("ACCEPTED"))
				.andDo(print());
		}

		@Test
		@DisplayName("실패(유효성 검증): 주문 상태(newStatus)가 누락된 요청은 400 Bad Request를 반환한다.")
		void updateOrderStatus_Fail_Validation() throws Exception {
			// given
			UUID orderId = UUID.randomUUID();
			UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
			request.setNewStatus(null);

			// when
			ResultActions resultActions = mockMvc.perform(patch("/customer/order/{orderId}/status", orderId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(ErrorStatus._BAD_REQUEST.getCode()))
				.andExpect(jsonPath("$.result.newStatus").exists())
				.andDo(print());
		}

		@Test
		@DisplayName("실패(비즈니스 로직): 권한 없는 사용자가 요청 시 403 Forbidden을 반환한다.")
		void updateOrderStatus_Fail_AccessDenied() throws Exception {
			// given
			UUID orderId = UUID.randomUUID();
			UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
			request.setNewStatus(OrderStatus.ACCEPTED);

			given(orderService.updateOrderStatus(eq(orderId), any(OrderStatus.class)))
				.willThrow(new GeneralException(OrderErrorStatus.ORDER_ACCESS_DENIED));

			// when
			ResultActions resultActions = mockMvc.perform(patch("/customer/order/{orderId}/status", orderId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			resultActions
				.andExpect(status().isForbidden()) // 💡 HTTP 403 상태 코드를 기대
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value(OrderErrorStatus.ORDER_ACCESS_DENIED.getCode()))
				.andDo(print());
		}
	}
}