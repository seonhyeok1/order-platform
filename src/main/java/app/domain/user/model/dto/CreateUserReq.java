package app.domain.user.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserReq {
	private String userName;
	private String password;
	private String email;
	private String nickname;
	private String phoneNumber;
}
