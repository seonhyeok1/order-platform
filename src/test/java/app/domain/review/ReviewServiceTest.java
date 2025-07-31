package app.domain.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.order.model.OrdersRepository;
import app.domain.order.model.entity.Orders;
import app.domain.review.model.ReviewRepository;
import app.domain.review.model.dto.request.CreateReviewRequest;
import app.domain.review.model.dto.response.CreateReviewResponse;
import app.domain.review.model.dto.response.CustomerReviewResponse;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.domain.review.model.entity.Review;
import app.domain.store.model.entity.Store;
import app.domain.store.model.entity.StoreRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.global.apiPayload.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private OrdersRepository ordersRepository;

	@InjectMocks
	private ReviewService reviewService;

	private User user;
	private Store store;
	private Orders order;
	private Review review;

	@BeforeEach
	void setUp() {
		user = User.builder().userId(1L).email("test@example.com").nickname("testuser").build();
		store = Store.builder().storeId(UUID.randomUUID()).storeName("teststore").build();
		order = Orders.builder().ordersId(UUID.randomUUID()).user(user).store(store).build();
		review = Review.builder()
			.reviewId(UUID.randomUUID())
			.user(user)
			.store(store)
			.orders(order)
			.rating(5)
			.content("Great!")
			.build();
	}

	@Test
	void createReview_Success() {
		when(userRepository.findByUserId(any())).thenReturn(Optional.of(user));
		when(ordersRepository.findById(any())).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrders(any())).thenReturn(false);
		when(reviewRepository.save(any())).thenReturn(review);

		CreateReviewRequest request = new CreateReviewRequest(order.getOrdersId(), 5L, "Great!");
		CreateReviewResponse response = reviewService.createReview(1L, request);

		assertNotNull(response);
		assertEquals(review.getReviewId(), response);
	}

	@Test
	void createReview_UserNotFound() {
		when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());

		CreateReviewRequest request = new CreateReviewRequest(order.getOrdersId(), 5L, "Great!");
		assertThrows(GeneralException.class, () -> reviewService.createReview(1L, request));
	}

	@Test
	void getCustomerReviews_Success() {
		when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
		when(reviewRepository.findByUser(any())).thenReturn(Collections.singletonList(review));

		CreateReviewRequest reviewRequest;
		List<GetReviewResponse> responses = reviewService.createReview(1L, reviewRequest);

		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals(review.getReviewId(), responses.get(0).reviewId());
	}

	@Test
	void getStoreReviews_Success() {
		when(storeRepository.findById(any())).thenReturn(Optional.of(store));
		when(reviewRepository.findByStore(any())).thenReturn(Collections.singletonList(review));

		List<CustomerReviewResponse> responses = reviewService.getStoreReviews(store.getStoreId());

		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals(review.getReviewId(), responses.get(0).reviewId());
	}
}
