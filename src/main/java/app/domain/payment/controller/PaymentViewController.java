package app.domain.payment.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentViewController {

	@Value("${TOSS_CLIENT_KEY}")
	private String tossClientKey;

	@GetMapping()
	public String checkout(Model model) {
		UUID test = UUID.randomUUID();
		System.out.println(test);
		model.addAttribute("orderId", test);
		model.addAttribute("totalPrice", 10000);
		model.addAttribute("tossClientKey", tossClientKey);

		return "checkout";
	}

	// @GetMapping("/checkout")
	// public String checkout(@Valid @RequestBody CheckoutRequest request, Model model) {
	// 	model.addAttribute("orderId", request.orderId());
	// 	model.addAttribute("totalPrice", request.totalPrice());
	// 	model.addAttribute("tossClientKey", tossClientKey);
	//
	// 	return "checkout";
	// }

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

	@GetMapping("/fail")
	public String fail(@RequestParam String code,
		@RequestParam String message,
		@RequestParam String orderId,
		Model model) {
		model.addAttribute("code", code);
		model.addAttribute("message", message);
		model.addAttribute("orderId", orderId);
		return "fail";
	}
}