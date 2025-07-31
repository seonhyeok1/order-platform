package app.domain.user.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LoginResponse(
	@Schema
	String accessToken,

	@Schema
	String refreshToken
) {
}