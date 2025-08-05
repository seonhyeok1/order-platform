package app.domain.manager.dto.response;

import java.time.LocalDateTime;

import app.domain.user.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerListResponse {

	private Long id;
	private String email;
	private String name;
	private LocalDateTime createdAt;

	public static GetCustomerListResponse from(User user) {
		return GetCustomerListResponse.builder()
			.id(user.getUserId())
			.email(user.getEmail())
			.name(user.getUsername())
			.createdAt(user.getCreatedAt())
			.build();
	}
}