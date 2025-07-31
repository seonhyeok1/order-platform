package app.domain.order.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.domain.order.model.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, UUID> {
}
