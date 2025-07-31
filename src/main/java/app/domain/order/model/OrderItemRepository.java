package app.domain.order.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.domain.order.model.entity.OrderItem;
import app.domain.order.model.entity.Orders;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
	List<OrderItem> findByOrders(Orders orders);
}
