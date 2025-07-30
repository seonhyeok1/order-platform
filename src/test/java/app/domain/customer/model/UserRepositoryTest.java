package app.domain.customer.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.enums.UserRole;
import app.global.config.JpaAuditingConfig;

@DataJpaTest // 인메모리 DB 사용
@Import(JpaAuditingConfig.class)
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	private User user;

	// 각 테스트 실행 전에 테스트용 User 객체를 생성하고 DB에 저장
	@BeforeEach
	void setUp() {
		user = User.builder()
			.username("testuser")
			.password("password123!")
			.email("test@example.com")
			.nickname("testnick")
			.realName("김테스트")
			.phoneNumber("01012345678")
			.userRole(UserRole.CUSTOMER)
			.build();
		entityManager.persistAndFlush(user); // 영속성 컨텍스트에 저장하고 즉시 DB에 반영
	}

	@Nested
	@DisplayName("findByUsername 메서드 테스트")
	class FindByUsernameTest {
		@Test
		@DisplayName("성공: 존재하는 username으로 조회 시, User 객체를 담은 Optional을 반환한다.")
		void findByUsername_ExistingUser_ReturnsOptionalOfUser() {
			// when
			Optional<User> foundUser = userRepository.findByUsername("testuser");

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());
		}

		@Test
		@DisplayName("실패: 존재하지 않는 username으로 조회 시, 빈 Optional을 반환한다.")
		void findByUsername_NonExistingUser_ReturnsEmptyOptional() {
			// when
			Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");

			// then
			assertThat(foundUser).isNotPresent();
		}
	}

	@Nested
	@DisplayName("existsBy... 메서드 테스트")
	class ExistsByMethodsTest {
		@Test
		@DisplayName("성공: 존재하는 username에 대해 true를 반환한다.")
		void existsByUsername_Existing_ReturnsTrue() {
			assertThat(userRepository.existsByUsername("testuser")).isTrue();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 username에 대해 false를 반환한다.")
		void existsByUsername_NonExisting_ReturnsFalse() {
			assertThat(userRepository.existsByUsername("nonexistentuser")).isFalse();
		}

		@Test
		@DisplayName("성공: 존재하는 email에 대해 true를 반환한다.")
		void existsByEmail_Existing_ReturnsTrue() {
			assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 email에 대해 false를 반환한다.")
		void existsByEmail_NonExisting_ReturnsFalse() {
			assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
		}

		@Test
		@DisplayName("성공: 존재하는 nickname에 대해 true를 반환한다.")
		void existsByNickname_Existing_ReturnsTrue() {
			assertThat(userRepository.existsByNickname("testnick")).isTrue();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 nickname에 대해 false를 반환한다.")
		void existsByNickname_NonExisting_ReturnsFalse() {
			assertThat(userRepository.existsByNickname("nonexistentnick")).isFalse();
		}

		@Test
		@DisplayName("성공: 존재하는 phoneNumber에 대해 true를 반환한다.")
		void existsByPhoneNumber_Existing_ReturnsTrue() {
			assertThat(userRepository.existsByPhoneNumber("01012345678")).isTrue();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 phoneNumber에 대해 false를 반환한다.")
		void existsByPhoneNumber_NonExisting_ReturnsFalse() {
			assertThat(userRepository.existsByPhoneNumber("01000000000")).isFalse();
		}
	}

	@Nested
	@DisplayName("Unique 제약조건 테스트")
	class UniqueConstraintTest {

		@Test
		@DisplayName("예외: 동일한 username으로 저장 시, DataIntegrityViolationException이 발생한다.")
		void save_DuplicateUsername_ThrowsException() {
			// given
			User duplicateUser = User.builder()
				.username("testuser") // 기존 user와 동일한 username
				.password("newpass").email("new@example.com").nickname("newnick")
				.realName("김새로").phoneNumber("01099998888").userRole(UserRole.CUSTOMER)
				.build();

			// when & then
			assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
				.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		@DisplayName("예외: 동일한 email로 저장 시, DataIntegrityViolationException이 발생한다.")
		void save_DuplicateEmail_ThrowsException() {
			// given
			User duplicateUser = User.builder()
				.username("newuser")
				.password("newpass").email("test@example.com") // 기존 user와 동일한 email
				.nickname("newnick").realName("김새로").phoneNumber("01099998888")
				.userRole(UserRole.CUSTOMER).build();

			// when & then
			assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
				.isInstanceOf(DataIntegrityViolationException.class);
		}
	}
}