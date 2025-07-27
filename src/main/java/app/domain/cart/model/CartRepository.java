package app.domain.cart.model;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.domain.cart.model.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID> {
	Optional<Cart> findByUser_UserId(Long userId);
}