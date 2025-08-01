package app.domain.payment;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import app.domain.order.model.entity.Orders;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.SecurityConfig;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PaymentService paymentService;

	@Test
	@DisplayName("결제 checkout - 성공")
	@WithMockUser
	void checkout_Success() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();
		Orders mockOrder = Orders.builder()
			.ordersId(orderId)
			.totalPrice(50000L)
			.build();

		given(paymentService.getOrderById(orderId)).willReturn(mockOrder);

		// when & then
		mockMvc.perform(get("/payment/checkout/{orderId}", orderId))
			.andExpect(status().isOk())
			.andExpect(view().name("checkout"))
			.andExpect(model().attribute("orderId", orderId.toString()))
			.andExpect(model().attribute("totalPrice", 50000L));
	}

	@Test
	@DisplayName("결제 checkout - 주문 없음")
	@WithMockUser
	void checkout_OrderNotFound() throws Exception {
		// given
		UUID orderId = UUID.randomUUID();

		// when & then
		when(paymentService.getOrderById(orderId))
			.thenThrow(new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		mockMvc.perform(get("/payment/checkout/{orderId}", orderId))
			.andExpect(status().isNotFound());
	}
}