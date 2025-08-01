package app.domain.review.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import app.domain.order.model.entity.Orders;
import app.domain.review.model.entity.Review;
import app.domain.user.model.entity.User;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

	List<Review> findByUser(User user);

	boolean existsByOrders(Orders orders);
}
