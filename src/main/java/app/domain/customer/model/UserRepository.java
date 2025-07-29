package app.domain.customer.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Page<User> findAllByRole(UserRole role, Pageable pageable);
}
