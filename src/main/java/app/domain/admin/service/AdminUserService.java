package app.domain.admin.service;

import org.springframework.data.domain.Pageable;

import app.domain.admin.model.dto.response.GetUserListResponse;
import app.global.apiPayload.PagedResponse;

public interface AdminUserService {

	PagedResponse<GetUserListResponse> getAllUsers(Pageable pageable);

}
