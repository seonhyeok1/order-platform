package app.domain.payment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import app.domain.cart.service.CartService;
import app.domain.order.model.OrdersRepository;
import app.domain.order.model.entity.Orders;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.payment.model.PaymentEtcRepository;
import app.domain.payment.model.PaymentRepository;
import app.domain.payment.model.dto.request.CancelPaymentRequest;
import app.domain.payment.model.dto.request.PaymentConfirmRequest;
import app.domain.payment.model.dto.request.PaymentFailRequest;
import app.domain.payment.model.entity.Payment;
import app.domain.payment.model.entity.PaymentEtc;
import app.domain.payment.model.entity.enums.PaymentStatus;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	@Value("${TOSS_SECRET_KEY}")
	private String tossSecretKey;
	@Value("{TOSS_URL}")
	private String tossUrl;

	private final OrdersRepository ordersRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentEtcRepository paymentEtcRepository;
	private final CartService cartService;

	public Orders getOrderById(UUID orderId) {
		return ordersRepository.findById(orderId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));
	}

	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null
			&& authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
			org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User)authentication.getPrincipal();
			return Long.parseLong(user.getUsername());
		}
		throw new GeneralException(ErrorStatus._UNAUTHORIZED);
	}

	private String generateIdempotencyKey(Long userId, String orderId) {
		try {
			String input = userId + orderId;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public String confirmPayment(PaymentConfirmRequest request) {

		try {
			Orders order = getOrderById(UUID.fromString(request.orderId()));
			long requestAmount = Long.parseLong(request.amount());
			if (order.getTotalPrice() != requestAmount) {
				throw new GeneralException(ErrorStatus.PAYMENT_AMOUNT_MISMATCH);
			}
			String widgetSecretKey = tossSecretKey;
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
			String authorizations = "Basic " + new String(encodedBytes);

			String responseBody;
			boolean isSuccess;
			try {
				Long userId = getCurrentUserId();
				String idempotencyKey = generateIdempotencyKey(userId, request.orderId());

				JSONObject obj = new JSONObject();
				obj.put("orderId", request.orderId());
				obj.put("amount", request.amount());
				obj.put("paymentKey", request.paymentKey());

				URL url = new URL(tossUrl + "/confirm");
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("Authorization", authorizations);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Idempotency-Key", idempotencyKey);
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);

				OutputStream outputStream = connection.getOutputStream();
				outputStream.write(obj.toString().getBytes("UTF-8"));

				int code = connection.getResponseCode();
				isSuccess = code == 200;

				InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(responseStream, StandardCharsets.UTF_8));
				StringBuilder responseBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					responseBuilder.append(line);
				}
				responseBody = responseBuilder.toString();
			} catch (Exception e) {
				throw new GeneralException(ErrorStatus.TOSS_API_ERROR);
			}

			JSONObject responseJson = new JSONObject(responseBody);
			PaymentStatus paymentStatus = isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

			Payment payment = Payment.builder()
				.ordersId(order.getOrdersId())
				.paymentKey(request.paymentKey())
				.paymentMethod(order.getPaymentMethod())
				.paymentStatus(paymentStatus)
				.amount(order.getTotalPrice())
				.build();

			Payment savedPayment = paymentRepository.save(payment);

			PaymentEtc paymentEtc = PaymentEtc.builder()
				.payment(savedPayment)
				.paymentResponse(responseJson.toMap())
				.build();

			paymentEtcRepository.save(paymentEtc);

			if (isSuccess) {
				Long userId = getCurrentUserId();
				cartService.clearCartItems(userId);
				return "결제 승인이 완료되었습니다. PaymentKey: " + request.paymentKey();
			} else {
				throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
			}
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public String failSave(PaymentFailRequest request) {
		try {
			Orders order = getOrderById(request.orderId());

			Payment payment = Payment.builder()
				.ordersId(order.getOrdersId())
				.paymentKey("FAILED_" + System.currentTimeMillis())
				.paymentMethod(order.getPaymentMethod())
				.paymentStatus(PaymentStatus.FAILED)
				.amount(order.getTotalPrice())
				.build();

			Payment savedPayment = paymentRepository.save(payment);

			PaymentEtc paymentEtc = PaymentEtc.builder()
				.payment(savedPayment)
				.paymentResponse(Map.of(
					"errorCode", request.errorCode(),
					"message", request.message()
				))
				.build();

			paymentEtcRepository.save(paymentEtc);

			return "결제 실패 처리가 완료되었습니다.";
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public String cancelPayment(CancelPaymentRequest request) {
		try {
			Orders order = getOrderById(request.getOrderId());
			Payment payment = paymentRepository.findByOrdersId(request.getOrderId())
				.orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

			String widgetSecretKey = tossSecretKey;
			Base64.Encoder encoder = Base64.getEncoder();
			byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
			String authorizations = "Basic " + new String(encodedBytes);

			String responseBody;
			boolean isSuccess;
			try {
				Long userId = getCurrentUserId();
				String idempotencyKey = generateIdempotencyKey(userId, request.getOrderId().toString());

				JSONObject obj = new JSONObject();
				obj.put("cancelReason", request.getCancelReason());

				URL url = new URL(tossUrl + "/payments/" + payment.getPaymentKey() + "/cancel");
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("Authorization", authorizations);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Idempotency-Key", idempotencyKey);
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);

				OutputStream outputStream = connection.getOutputStream();
				outputStream.write(obj.toString().getBytes("UTF-8"));

				int code = connection.getResponseCode();
				isSuccess = code == 200;

				InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(responseStream, StandardCharsets.UTF_8));
				StringBuilder responseBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					responseBuilder.append(line);
				}
				responseBody = responseBuilder.toString();
			} catch (Exception e) {
				throw new GeneralException(ErrorStatus.TOSS_API_ERROR);
			}

			JSONObject responseJson = new JSONObject(responseBody);

			if (isSuccess) {
				order.updateOrderStatus(OrderStatus.REFUNDED);
				order.addHistory("cancel", LocalDateTime.now());

				payment.updatePaymentStatus(PaymentStatus.CANCELLED);
			}

			PaymentEtc paymentEtc = PaymentEtc.builder()
				.payment(payment)
				.paymentResponse(responseJson.toMap())
				.build();

			paymentEtcRepository.save(paymentEtc);

			if (isSuccess) {
				return "결제 취소가 완료되었습니다.";
			} else {
				throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
			}
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
		}
	}
}