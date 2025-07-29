package app.domain.admin.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import app.domain.admin.model.dto.response.GetUserListResponse;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.PagedResponse;

class AdminUserServiceImplTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final AdminUserServiceImpl adminUserService = new AdminUserServiceImpl(userRepository);

	@Test
	@DisplayName("관리자가 유저 목록을 조회한다")
	void getAllUsersTest() {
		// given
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		User user = User.builder()
			.userId(1L)
			.email("test@example.com")
			.username("테스트")
			.build();
		ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

		Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
		when(userRepository.findAllByUserRole(UserRole.CUSTOMER, pageable)).thenReturn(userPage);

		// when
		PagedResponse<GetUserListResponse> response = adminUserService.getAllUsers(pageable);

		// then
		assertThat(response.content().get(0).email()).isEqualTo("test@example.com");
		verify(userRepository, times(1)).findAllByUserRole(UserRole.CUSTOMER, pageable);
	}
}
