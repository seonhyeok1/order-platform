package app.domain.payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.payment.PaymentService;
import app.domain.payment.model.dto.request.PaymentConfirmRequest;
import app.domain.payment.model.dto.request.PaymentFailRequest;
import app.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/confirm")
	public ApiResponse<String> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
		String result = paymentService.confirmPayment(request);
		return ApiResponse.onSuccess(result);
	}

	@PostMapping("/fail")
	public ApiResponse<String> processFail(@Valid @RequestBody PaymentFailRequest request) {
		String result = paymentService.failSave(request);
		return ApiResponse.onSuccess(result);
	}
}