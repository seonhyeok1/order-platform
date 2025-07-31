package app.domain.user.model;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String userName);

	Page<User> findAllByUserRole(UserRole role, Pageable pageable);

	Optional<User> findByUserId(Long userId);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByPhoneNumber(String phoneNumber);

}