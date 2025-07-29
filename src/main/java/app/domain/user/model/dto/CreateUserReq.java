package app.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserReq {

	@NotBlank(message = "사용자 아이디는 필수입니다.")
	@Size(min = 4, max = 20, message = "사용자 아이디는 4자 이상 20자 이하로 입력해주세요.")
	@Schema(description = "사용자 아이디", example = "testuser1")
	private String username;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
	@Schema(description = "비밀번호", example = "password123!")
	private String password;

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	@Schema(description = "이메일", example = "test@example.com")
	private String email;

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
	@Schema(description = "닉네임", example = "테스트유저")
	private String nickname;

	@NotBlank(message = "실명은 필수입니다.")
	@Size(min = 2, max = 50, message = "실명은 2자 이상 50자 이하로 입력해주세요.")
	@Schema(description = "실명", example = "김구름")
	private String realName;

	@NotBlank(message = "전화번호는 필수입니다.")
	@Pattern(regexp = "^\\d{10,11}$", message = "유효한 전화번호 형식이 아닙니다. ('-' 없이 숫자만 입력)")
	@Schema(description = "전화번호('-' 없이 숫자만 입력)", example = "01012345678")
	private String phoneNumber;
}