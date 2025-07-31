package app.domain.user.model.dto;

import app.domain.user.model.entity.enums.UserRole;
import app.global.validation.annotation.ValidUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserReq {

	@NotBlank
	@Size(min = 4, max = 20, message = "사용자 아이디는 4자 이상 20자 이하로 입력해주세요.")
	@Schema(description = "사용자 아이디", example = "testuser1")
	private String username;

	@NotBlank
	@Size(min = 8, max = 20)
	@Schema(description = "비밀번호", example = "password123!")
	private String password;

	@NotBlank
	@Email
	@Schema(description = "이메일", example = "test@example.com")
	private String email;

	@NotBlank
	@Size(min = 2, max = 10)
	@Schema(description = "닉네임", example = "테스트유저")
	private String nickname;

	@NotBlank
	@Size(min = 2, max = 50)
	@Schema(description = "실명", example = "김구름")
	private String realName;

	@NotBlank
	@Pattern(regexp = "^\\d{10,11}$")
	@Schema(description = "전화번호('-' 없이 숫자만 입력)", example = "01012345678")
	private String phoneNumber;

	@NotNull
	@ValidUserRole
	@Schema(description = "사용자 권한", example = "CUSTOMER")
	private UserRole userRole;
}