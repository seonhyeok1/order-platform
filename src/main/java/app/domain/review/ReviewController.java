package app.domain.review;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.domain.review.model.dto.request.CreateReviewRequest;
import app.domain.review.model.dto.response.GetReviewResponse;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "review", description = "리뷰 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/customer/review")
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(summary = "리뷰 작성 API", description = "고객이 상품에 대한 리뷰를 작성합니다.")
	@Parameters({
		@Parameter(name = "userId", description = "사용자 ID")
	})
	@PostMapping
	public ApiResponse<String> createReview(
		@RequestParam Long userId,
		@Valid @RequestBody CreateReviewRequest request
	) {
		return ApiResponse.onSuccess(reviewService.createReview(userId, request));
	}

	@Operation(summary = "리뷰 조회 API", description = "특정 고객 또는 특정 가게의 리뷰 내역을 조회합니다.")
	@Parameters({
		@Parameter(name = "userId", description = "사용자 ID"),
		@Parameter(name = "storeId", description = "조회할 가게의 ID")
	})
	@GetMapping
	public ApiResponse<List<GetReviewResponse>> getReviews(
		@RequestParam(name = "userId", required = false) Long userId,
		@RequestParam(name = "storeId", required = false) UUID storeId
	) {
		return ApiResponse.onSuccess(reviewService.getReviews(userId, storeId));
	}
}
