package app.domain.manager.dto.response;

import java.time.LocalDateTime;

import app.domain.user.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 유저 목록 응답 DTO")
public record GetCustomListResponse(
	Long id,
	String email,
	String name,
	LocalDateTime createdAt
) {
	public static GetCustomListResponse from(User user) {
		return new GetCustomListResponse(
			user.getUserId(),
			user.getEmail(),
			user.getUsername(),
			user.getCreatedAt()
		);
	}
}