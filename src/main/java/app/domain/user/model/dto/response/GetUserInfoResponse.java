package app.domain.user.model.dto.response;

import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserInfoResponse {

	private Long userId;
	private String username;
	private String email;
	private String nickname;
	private String realName;
	private String phoneNumber;
	private UserRole userRole;

	public static GetUserInfoResponse from(User user) {
		return GetUserInfoResponse.builder()
			.userId(user.getUserId())
			.username(user.getUsername())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.realName(user.getRealName())
			.phoneNumber(user.getPhoneNumber())
			.userRole(user.getUserRole())
			.build();
	}
}
