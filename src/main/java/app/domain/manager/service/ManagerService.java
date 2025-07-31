package app.domain.manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.enums.UserRole;
import app.domain.manager.dto.response.GetUserListResponse;
import app.global.apiPayload.PagedResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

	private final UserRepository userRepository;

	public PagedResponse<GetUserListResponse> getAllUsers(Pageable pageable) {
		Page<GetUserListResponse> page = userRepository.findAllByUserRole(UserRole.CUSTOMER, pageable)
			.map(GetUserListResponse::from);

		return PagedResponse.from(page);
	}
}