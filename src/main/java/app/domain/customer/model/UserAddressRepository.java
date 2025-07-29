package app.domain.customer.model;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import app.domain.customer.model.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
}