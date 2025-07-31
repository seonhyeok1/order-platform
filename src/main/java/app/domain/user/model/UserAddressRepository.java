package app.domain.user.model;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import app.domain.user.model.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
	List<UserAddress> findAllByUserUserId(Long UserId);
	Optional<UserAddress> findByUser_UserIdAndIsDefaultTrue(Long userId);
}