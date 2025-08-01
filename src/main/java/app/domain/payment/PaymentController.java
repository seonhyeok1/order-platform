package app.domain.payment;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.domain.payment.model.dto.request.PaymentConfirmRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

	private final PaymentService paymentService;

	@GetMapping()
	public String checkout(Model model) {
		// Orders order = paymentService.getOrderById(orderId);
		UUID test = UUID.randomUUID();
		model.addAttribute("orderId", test);
		model.addAttribute("totalPrice", 13000);

		return "checkout";
	}

	@GetMapping("/success")
	public String success(@RequestParam String paymentKey,
		@RequestParam String orderId,
		@RequestParam String amount,
		Model model) {
		model.addAttribute("paymentKey", paymentKey);
		model.addAttribute("orderId", orderId);
		model.addAttribute("amount", amount);
		return "success";
	}

	@PostMapping("/confirm")
	public ResponseEntity<String> confirm(@RequestBody PaymentConfirmRequest request) {
		String result = paymentService.confirmPayment(request);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/fail")
	public String fail() {
		return "fail";
	}
}