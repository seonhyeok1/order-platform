package app.domain.manager.service;

import org.springframework.data.domain.Pageable;

import app.domain.manager.model.dto.response.GetUserListResponse;
import app.global.apiPayload.PagedResponse;

public interface AdminUserService {

	PagedResponse<GetUserListResponse> getAllUsers(Pageable pageable);

}
