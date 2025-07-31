package app.domain.review;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.domain.review.model.dto.request.CreateReviewRequest;
import app.domain.review.model.dto.response.CreateReviewResponse;
import app.domain.review.model.dto.response.CustomerReviewResponse;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReviewService reviewService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
    void createReview() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(UUID.randomUUID(), 5, "Great!");
        CreateReviewResponse response = new CreateReviewResponse(UUID.randomUUID(), "리뷰가 성공적으로 작성되었습니다.");

        when(reviewService.createReview(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/review?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.reviewId").exists());
    }

    @Test
    void getReviews_Customer() throws Exception {
        CustomerReviewResponse response = CustomerReviewResponse.builder().reviewId(UUID.randomUUID()).build();
        when(reviewService.getCustomerReviews(any())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/review?userId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].reviewId").exists());
    }

    @Test
    void getReviews_Store() throws Exception {
        UUID storeId = UUID.randomUUID();
        CustomerReviewResponse response = CustomerReviewResponse.builder().reviewId(UUID.randomUUID()).build();
        when(reviewService.getStoreReviews(any())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/review").param("storeId", storeId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].reviewId").exists());
    }
}
