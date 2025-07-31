package app.domain.review;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.domain.order.model.OrdersRepository;
import app.domain.order.model.entity.Orders;
import app.domain.review.model.ReviewRepository;
import app.domain.review.model.dto.request.CreateReviewRequest;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.domain.review.model.entity.Review;
import app.domain.store.model.StoreRepository;
import app.domain.store.model.entity.Store;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 기본 설정
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final OrdersRepository ordersRepository;

	@Transactional
	public String createReview(Long userId, CreateReviewRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

		Orders order = ordersRepository.findById(request.orderId())
			.orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_NOT_FOUND));

		if (!order.getUser().equals(user)) {
			throw new GeneralException(ErrorStatus._FORBIDDEN);
		}

		if (reviewRepository.existsByOrders(order)) {
			throw new GeneralException(ErrorStatus.REVIEW_ALREADY_EXISTS);
		}

		Review review = Review.builder()
			.user(user)
			.store(order.getStore())
			.orders(order)
			.rating(request.rating())
			.content(request.content())
			.build();

		Review savedReview = reviewRepository.save(review);

		return savedReview.getReviewId() + " 가 생성되었습니다.";
	}

	public List<GetReviewResponse> getReviews(Long userId, UUID storeId) {
		List<Review> reviews;

		if (userId != null) {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
			reviews = reviewRepository.findByUser(user);
		} else if (storeId != null) {
			Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));
			reviews = reviewRepository.findByStore(store);
		} else {
			reviews = Collections.emptyList();
		}

		if (reviews.isEmpty()) {
			throw new GeneralException(ErrorStatus.REVIEW_NOT_FOUND);
		}

		return reviews.stream()
			.map(GetReviewResponse::from)
			.toList();
	}
}