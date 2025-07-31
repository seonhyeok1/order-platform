package app.domain.auth.model.dto.response;

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