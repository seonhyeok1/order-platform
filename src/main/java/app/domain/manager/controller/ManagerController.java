package app.domain.manager.controller;

import static org.springframework.data.domain.Sort.Direction.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.domain.manager.dto.response.GetUserListResponse;
import app.domain.manager.service.ManagerService;
import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "관리자 API", description = "관리자만 이용할 수 있는 API")
public class ManagerController {

	private final ManagerService managerService;

	@GetMapping
	@Operation(
		summary = "전체 유저 목록 조회",
		description = "가입한 유저 목록을 페이지 별로 조회합니다. 생성일 또는 수정일 기준으로 정렬 할수 있습ㅈ니다."
	)
	public ApiResponse<PagedResponse<GetUserListResponse>> getAllUsers(
		@PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
	) {
		return ApiResponse.onSuccess(managerService.getAllUsers(pageable));
	}
}