package app.domain.user.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank
	@Schema(description = "사용자 아이디", example = "testuser")
	String username,

	@NotBlank
	@Schema(description = "사용자 비밀번호", example = "password123!")
	String password
) {
}