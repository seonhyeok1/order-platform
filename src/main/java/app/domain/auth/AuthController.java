package app.domain.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.customer.model.dto.CreateUserReq;
import app.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 및 로그인, 회원가입")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	/*-------------------------------------------
	 *
	 *      회원가입 - 계정 생성(customer, owner)
	 *
	 *-------------------------------------------*/

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
					        "username": "사용자 아이디는 4자 이상 20자 이하로 입력해주세요."
					    }
					}
					"""))),
		// 2. AuthService에서 발생하는 중복 에러(409 Conflict)를 Swagger 문서에 추가
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "CONFLICT, 데이터 중복",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				// 3. 각 에러 케이스별로 상세한 예시를 추가하여 API 사용자가 이해하기 쉽게 함
				examples = {
					@ExampleObject(name = "아이디 중복 (USER002)", value = """
						{
						    "resultCode": "USER002",
						    "message": "이미 존재하는 유저입니다.",
						    "result": null
						}
						"""),
					@ExampleObject(name = "이메일 중복 (USER003)", value = """
						{
						    "resultCode": "USER003",
						    "message": "이미 사용 중인 이메일입니다.",
						    "result": null
						}
						"""),
					@ExampleObject(name = "닉네임 중복 (USER004)", value = """
						{
						    "resultCode": "USER004",
						    "message": "이미 사용 중인 닉네임입니다.",
						    "result": null
						}
						"""),
					@ExampleObject(name = "전화번호 중복 (USER005)", value = """
						{
						    "resultCode": "USER005",
						    "message": "이미 사용 중인 전화번호입니다.",
						    "result": null
						}
						""")
				}))
	})
	@PostMapping("/signup")
	public ApiResponse<String> createUser(@Valid @RequestBody CreateUserReq createUserReq) {
		String userId = authService.createUser(createUserReq);
		return ApiResponse.onSuccess(userId);
	}
}