package app.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.user.model.dto.CreateUserReq;
import app.domain.user.service.UserService;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "user", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	@Operation(summary = "회원가입 API", description = "새로운 사용자를 등록합니다. 모든 필드는 유효성 검사를 거칩니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK, 성공. 생성된 사용자의 ID(Long)를 문자열로 반환합니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BAD_REQUEST, 입력 값 유효성 검증 실패",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{
					    "resultCode": "COMMON400",
					    "message": "잘못된 요청입니다.",
					    "result": {
					        "username": "사용자 아이디는 필수입니다."
					    }
					}
					""")))
	})
	@PostMapping("/signup")
	public ApiResponse<String> createUser(@Valid @RequestBody CreateUserReq createUserReq) {
		String userId = userService.createUser(createUserReq);
		return ApiResponse.onSuccess(userId);
	}
}