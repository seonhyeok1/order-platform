package app.domain.review;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
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

import app.domain.review.model.dto.request.CreateReviewRequest;
import app.domain.review.model.dto.request.GetReviewRequest;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.SecurityConfig;
import app.global.jwt.JwtAccessDeniedHandler;
import app.global.jwt.JwtAuthenticationEntryPoint;
import app.global.jwt.JwtTokenProvider;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
@DisplayName("ReviewController 테스트")
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ReviewService reviewService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockitoBean
	private JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Test
	@DisplayName("리뷰 생성 - 성공")
	@WithMockUser
	void createReview_Success() throws Exception {
		// given
		CreateReviewRequest request = new CreateReviewRequest(1L, UUID.randomUUID(), 5L, "맛있어요");
		String successMessage = "오더 : " + request.orderId() + "에 대한 리뷰가 생성되었습니다.";
		when(reviewService.createReview(any(CreateReviewRequest.class))).thenReturn(successMessage);

		// when & then
		mockMvc.perform(post("/customer/review")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").value(successMessage));
	}

	@Test
	@DisplayName("리뷰 생성 - 실패 (서비스 예외)")
	@WithMockUser
	void createReview_Fail_ServiceException() throws Exception {
		// given
		CreateReviewRequest request = new CreateReviewRequest(1L, UUID.randomUUID(), 5L, "맛있어요");
		when(reviewService.createReview(any(CreateReviewRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus.REVIEW_ALREADY_EXISTS));

		// when & then
		mockMvc.perform(post("/customer/review")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.resultCode").value("REVIEW001"))
			.andExpect(jsonPath("$.message").value("이미 해당 주문에 대한 리뷰가 존재합니다."));
	}

	@Test
	@DisplayName("리뷰 조회 - 성공")
	@WithMockUser
	void getReviews_Success() throws Exception {
		// given
		GetReviewRequest request = new GetReviewRequest(1L);
		List<GetReviewResponse> responseList = Collections.singletonList(
			new GetReviewResponse(UUID.randomUUID(), "testuser", "teststore", 5L, "Great!", LocalDateTime.now())
		);
		when(reviewService.getReviews(any(GetReviewRequest.class))).thenReturn(responseList);

		// when & then
		mockMvc.perform(get("/customer/review")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.result").isArray())
			.andExpect(jsonPath("$.result.length()").value(1))
			.andExpect(jsonPath("$.result[0].customerName").value("testuser"));
	}

	@Test
	@DisplayName("리뷰 조회 - 실패 (사용자 없음)")
	@WithMockUser
	void getReviews_Fail_UserNotFound() throws Exception {
		// given
		GetReviewRequest request = new GetReviewRequest(999L);
		when(reviewService.getReviews(any(GetReviewRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus.USER_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/customer/review")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.resultCode").value("USER001"))
			.andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
	}

	@Test
	@DisplayName("리뷰 조회 - 실패 (리뷰 없음)")
	@WithMockUser
	void getReviews_Fail_NoReviewsFound() throws Exception {
		// given
		GetReviewRequest request = new GetReviewRequest(1L);
		when(reviewService.getReviews(any(GetReviewRequest.class)))
			.thenThrow(new GeneralException(ErrorStatus.NO_REVIEWS_FOUND_FOR_USER));

		// when & then
		mockMvc.perform(get("/customer/review")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.resultCode").value("REVIEW001"))
			.andExpect(jsonPath("$.message").value("해당 사용자가 작성한 리뷰가 없습니다."));
	}
}