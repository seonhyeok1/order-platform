package app.domain.admin.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.domain.admin.model.dto.response.GetUserListResponse;
import app.domain.admin.service.AdminUserService;
import app.domain.customer.model.UserRepository;
import app.domain.customer.model.entity.enums.UserRole;
import app.global.apiPayload.PagedResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final UserRepository userRepository;

	@Override
	public PagedResponse<GetUserListResponse> getAllUsers(Pageable pageable) {
		Page<GetUserListResponse> page = userRepository.findAllByRole(UserRole.USER, pageable)
			.map(GetUserListResponse::from);

		return PagedResponse.from(page);
	}
}