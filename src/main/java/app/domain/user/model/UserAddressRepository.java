package app.domain.user.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.domain.user.model.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
}