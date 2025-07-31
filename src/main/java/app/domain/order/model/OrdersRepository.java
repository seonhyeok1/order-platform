package app.domain.order.model;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.order.model.entity.Orders;
import app.domain.user.model.entity.User;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, UUID> {
	Page<Orders> findAllByUserAndDeliveryAddressIsNotNull(User user, Pageable pageable);
}
